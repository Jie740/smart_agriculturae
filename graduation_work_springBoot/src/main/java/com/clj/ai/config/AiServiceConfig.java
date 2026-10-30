package com.clj.ai.config;

import com.clj.ai.service.AssistantService;
import com.clj.ai.service.PendingUserMessageContext;
import com.clj.ai.store.PersistentChatMemoryStore;
import com.clj.ai.tool.RagSearchTool;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Service 配置
 * 通过 AiServices 动态代理构建 AssistantService，并注册 RAG 检索工具，
 * 由模型自主判断何时调用 listKnowledgeBases / searchKnowledge。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiServiceConfig {

    private final RagSearchTool ragSearchTool;
    private final PersistentChatMemoryStore chatMemoryStore;

    @Bean
    public AssistantService assistantService(
            ChatModel chatModel,
            StreamingChatModel streamingChatModel
    ) {
        return AiServices.builder(AssistantService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                // 附件图片无法通过方法参数传入（会被当成模板字符串），
                // 这里在请求真正发给模型前把最后一条用户消息替换为含图片的多模态消息
                .chatRequestTransformer((request, memoryId) -> {
                    UserMessage pending = PendingUserMessageContext.consume(String.valueOf(memoryId));
                    if (pending == null) {
                        return request;
                    }
                    List<ChatMessage> messages = new ArrayList<>(request.messages());
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        if (messages.get(i) instanceof UserMessage) {
                            messages.set(i, pending);
                            log.info("已注入多模态用户消息（含 {} 个内容块），memoryId={}",
                                    pending.contents().size(), memoryId);
                            return request.toBuilder().messages(messages).build();
                        }
                    }
                    return request;
                })
                .tools(ragSearchTool)
                .build();
    }
}
