package com.clj.ai.store;

import com.clj.ai.domain.BizMessage;
import com.clj.ai.service.BizConversationService;
import com.clj.ai.service.BizMessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将 LangChain4j 的会话记忆绑定到当前用户的当前会话。
 *
 * memoryId 格式固定为 userId:conversationId，禁止使用单例共享记忆。
 * DB 展示消息仍由 ChatController 写入，这里只负责读取最近 10 条供模型上下文使用。
 */
@Component
@RequiredArgsConstructor
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private static final int MAX_MEMORY_MESSAGES = 10;
    private final Map<String, List<ChatMessage>> activeMemories = new ConcurrentHashMap<>();

    private final BizConversationService conversationService;
    private final BizMessageService messageService;
    private final ObjectMapper objectMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String cacheKey = String.valueOf(memoryId);
        List<ChatMessage> cached = activeMemories.get(cacheKey);
        if (cached != null) {
            return new ArrayList<>(cached);
        }
        MemoryKey key = parse(memoryId);
        if (key == null || conversationService.getOwned(key.conversationId(), key.userId()) == null) {
            return List.of();
        }

        List<ChatMessage> messages = new ArrayList<>();
        for (BizMessage message : messageService.listRecentByConversationId(
                key.conversationId(), MAX_MEMORY_MESSAGES)) {
            String text = extractText(message.getContentJson());
            if (text.isBlank()) {
                continue;
            }
            if ("USER".equalsIgnoreCase(message.getRole())) {
                messages.add(UserMessage.from(text));
            } else if ("AI".equalsIgnoreCase(message.getRole())
                    || "ASSISTANT".equalsIgnoreCase(message.getRole())) {
                messages.add(AiMessage.from(text));
            }
        }
        activeMemories.put(cacheKey, new ArrayList<>(messages));
        return messages;
    }

    /**
     * 本项目由 ChatController 统一写入带附件和检索范围的展示消息。
     * LangChain4j 的临时窗口只在本次调用内更新，避免再次写入造成重复消息。
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (memoryId == null) {
            return;
        }
        int fromIndex = Math.max(0, messages.size() - MAX_MEMORY_MESSAGES);
        activeMemories.put(String.valueOf(memoryId), new ArrayList<>(messages.subList(fromIndex, messages.size())));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        evict(memoryId);
    }

    public void evict(Object memoryId) {
        if (memoryId != null) {
            activeMemories.remove(String.valueOf(memoryId));
        }
    }

    private String extractText(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return "";
        }
        try {
            JsonNode node = objectMapper.readTree(contentJson);
            JsonNode text = node.get("text");
            if (text != null && !text.isNull()) {
                return text.asText();
            }
            JsonNode contents = node.get("contents");
            if (contents != null && contents.isArray()) {
                StringBuilder result = new StringBuilder();
                contents.forEach(item -> {
                    JsonNode itemText = item.get("text");
                    if (itemText != null && !itemText.isNull()) {
                        if (result.length() > 0) {
                            result.append('\n');
                        }
                        result.append(itemText.asText());
                    }
                });
                return result.toString();
            }
        } catch (Exception ignored) {
            return contentJson;
        }
        return contentJson;
    }

    private MemoryKey parse(Object memoryId) {
        if (memoryId == null) {
            return null;
        }
        String value = String.valueOf(memoryId);
        int separator = value.indexOf(':');
        if (separator <= 0 || separator == value.length() - 1) {
            return null;
        }
        try {
            return new MemoryKey(
                    Long.parseLong(value.substring(0, separator)),
                    Long.parseLong(value.substring(separator + 1)));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record MemoryKey(Long userId, Long conversationId) {
    }
}
