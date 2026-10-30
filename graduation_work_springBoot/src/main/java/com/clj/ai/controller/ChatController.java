package com.clj.ai.controller;

import com.clj.ai.domain.BizMessage;
import com.clj.ai.dto.ChatAttachmentDto;
import com.clj.ai.dto.ChatRequestDto;
import com.clj.ai.service.AssistantService;
import com.clj.ai.service.BizConversationService;
import com.clj.ai.service.BizMessageService;
import com.clj.ai.service.PendingUserMessageContext;
import com.clj.ai.service.RagSelectionContext;
import com.clj.ai.store.PersistentChatMemoryStore;
import com.clj.ai.util.MessageUtil;
import com.clj.common.result.Result;
import com.clj.domain.BizConversation;
import com.clj.security.util.SecurityUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Date;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {

    private final AssistantService assistantService;
    private final MessageUtil messageUtil;
    private final BizConversationService conversationService;
    private final BizMessageService messageService;
    private final ObjectMapper objectMapper;
    private final PersistentChatMemoryStore chatMemoryStore;

    @GetMapping("/conversations")
    public Result<List<BizConversation>> listConversations() {
        return Result.success(conversationService.listByUserId(SecurityUtil.getUserId()));
    }

    @PostMapping("/conversations")
    public Result<BizConversation> createConversation(@RequestBody(required = false) Map<String, Object> body) {
        Long userId = SecurityUtil.getUserId();
        String title = body == null ? null : asString(body.get("title"));
        String model = body == null ? null : asString(body.get("modelName"));
        return Result.success(conversationService.create(userId, title, model));
    }

    @PutMapping("/conversations/{id}")
    public Result<Void> updateConversation(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        conversationService.updateTitle(id, SecurityUtil.getUserId(), asString(body.get("title")));
        return Result.success();
    }

    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(@PathVariable Long id) {
        conversationService.deleteOwned(id, SecurityUtil.getUserId());
        return Result.success();
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<List<BizMessage>> listMessages(@PathVariable Long id) {
        Long userId = SecurityUtil.getUserId();
        requireConversation(id, userId);
        return Result.success(messageService.listByConversationId(id));
    }

    @DeleteMapping("/messages/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteOwned(id, SecurityUtil.getUserId(), conversationService);
        return Result.success();
    }

    @DeleteMapping("/messages/{id}/after")
    public Result<Void> deleteMessagesAfter(@PathVariable Long id) {
        messageService.deleteAfter(id, SecurityUtil.getUserId(), conversationService);
        return Result.success();
    }

    @PutMapping("/messages/{id}")
    public Result<Void> updateMessage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        messageService.replaceUserMessage(
                id,
                SecurityUtil.getUserId(),
                asString(body.get("text")),
                conversationService);
        return Result.success();
    }

    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody ChatRequestDto chatRequestDto) {
        Long userId = SecurityUtil.getUserId();
        String text = chatRequestDto.getText() == null ? "" : chatRequestDto.getText().trim();
        if (text.isBlank() && (chatRequestDto.getAttachments() == null || chatRequestDto.getAttachments().isEmpty())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        BizConversation conversation = resolveConversation(chatRequestDto, userId);
        String memoryId = userId + ":" + conversation.getId();
        SseEmitter emitter = new SseEmitter(0L);
        StringBuilder generated = new StringBuilder();
        AtomicBoolean cancelled = new AtomicBoolean(false);
        AtomicBoolean userSaved = new AtomicBoolean(chatRequestDto.getReplaceMessageId() != null);
        AtomicBoolean assistantSaved = new AtomicBoolean(false);

        Runnable saveUser = () -> {
            if (!userSaved.compareAndSet(false, true)) {
                return;
            }
            saveMessage(conversation.getId(), userId, "USER", text,
                    chatRequestDto.getAttachments(),
                    chatRequestDto.getKnowledgeBaseIds(),
                    chatRequestDto.getDocumentIds());
        };
        Runnable saveAssistant = () -> {
            saveUser.run();
            if (!assistantSaved.compareAndSet(false, true) || generated.isEmpty()) {
                messageService.retainLatest(conversation.getId(), userId, 10, conversationService);
                touchConversation(conversation, userId);
                chatMemoryStore.evict(memoryId);
                return;
            }
            saveMessage(conversation.getId(), userId, "AI", generated.toString(), null, null, null);
            messageService.retainLatest(conversation.getId(), userId, 10, conversationService);
            touchConversation(conversation, userId);
            chatMemoryStore.evict(memoryId);
        };
        Runnable finishRequest = () -> {
            try {
                saveAssistant.run();
            } catch (Exception error) {
                log.error("保存聊天消息失败, conversationId={}", conversation.getId(), error);
            } finally {
                RagSelectionContext.clear(memoryId);
                PendingUserMessageContext.clear(memoryId);
                chatMemoryStore.evict(memoryId);
            }
        };
        emitter.onCompletion(finishRequest);
        emitter.onTimeout(() -> {
            cancelled.set(true);
            finishRequest.run();
        });
        emitter.onError(error -> {
            cancelled.set(true);
            finishRequest.run();
        });

        try {
            if (chatRequestDto.getReplaceMessageId() != null) {
                messageService.replaceUserMessage(
                        chatRequestDto.getReplaceMessageId(),
                        userId,
                        text,
                        conversationService);
            }
            emitter.send(SseEmitter.event().name("conversation").data(conversation.getId().toString()));

            RagSelectionContext.set(
                    memoryId,
                    chatRequestDto.getKnowledgeBaseIds() == null ? List.of() : chatRequestDto.getKnowledgeBaseIds(),
                    chatRequestDto.getDocumentIds() == null ? List.of() : chatRequestDto.getDocumentIds());
            // 文本（含文档附件说明）作为普通用户消息发送；
            // 带图片时额外登记一条多模态消息，由 chatRequestTransformer 注入
            String modelText = messageUtil.buildText(chatRequestDto);
            if (messageUtil.hasImages(chatRequestDto)) {
                PendingUserMessageContext.set(memoryId, messageUtil.buildMultimodalMessage(chatRequestDto, modelText));
            }
            TokenStream tokenStream = assistantService.chatStream(memoryId, modelText);
            tokenStream
                    .onPartialResponse(token -> {
                        if (cancelled.get()) {
                            return;
                        }
                        try {
                            if (token != null && !token.isEmpty()) {
                                generated.append(token);
                                emitter.send(SseEmitter.event().data(token));
                            }
                        } catch (Exception e) {
                            cancelled.set(true);
                            emitter.completeWithError(e);
                        }
                    })
                    .beforeToolExecution(beforeToolExecution -> {
                        if (cancelled.get()) {
                            return;
                        }
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("tool_call")
                                    .data("正在检索知识库..."));
                        } catch (Exception e) {
                            cancelled.set(true);
                        }
                    })
                    .onCompleteResponse(response -> {
                        finishRequest.run();
                        if (!cancelled.get()) {
                            try {
                                emitter.send(SseEmitter.event().name("done").data(""));
                                emitter.complete();
                            } catch (Exception e) {
                                cancelled.set(true);
                                emitter.completeWithError(e);
                            }
                        }
                    })
                    .onError(error -> {
                        log.error("流式响应出错", error);
                        finishRequest.run();
                        if (!cancelled.get()) {
                            try {
                                emitter.send(SseEmitter.event().name("error").data("处理出错: " + error.getMessage()));
                            } catch (Exception ignored) {
                                // The client may have already closed the connection.
                            }
                            emitter.completeWithError(error);
                        }
                    })
                    .start();
        } catch (Exception e) {
            log.error("启动流式响应失败", e);
            finishRequest.run();
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private BizConversation resolveConversation(ChatRequestDto request, Long userId) {
        if (request.getConversationId() != null) {
            return requireConversation(request.getConversationId(), userId);
        }
        String text = request.getText() == null ? "" : request.getText().trim();
        String title = text.isBlank() ? "附件咨询" : (text.length() > 40 ? text.substring(0, 40) : text);
        return conversationService.create(userId, title, request.getModel());
    }

    private BizConversation requireConversation(Long id, Long userId) {
        BizConversation conversation = conversationService.getOwned(id, userId);
        if (conversation == null) {
            throw new IllegalArgumentException("会话不存在");
        }
        return conversation;
    }

    private void saveMessage(Long conversationId, Long userId, String role, String text,
                             List<ChatAttachmentDto> attachments,
                             List<Long> knowledgeBaseIds,
                             List<Long> documentIds) {
        BizMessage message = new BizMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("text", text == null ? "" : text);
        if (attachments != null && !attachments.isEmpty()) content.put("attachments", attachments);
        if (knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty()) content.put("knowledgeBaseIds", knowledgeBaseIds);
        if (documentIds != null && !documentIds.isEmpty()) content.put("documentIds", documentIds);
        message.setContentJson(toJson(content));
        message.setIsDeleted(false);
        message.setCreatedBy(userId);
        message.setUpdatedBy(userId);
        message.setCreatedTime(new Date());
        message.setUpdatedTime(new Date());
        messageService.save(message);
    }

    private void touchConversation(BizConversation conversation, Long userId) {
        conversation.setUpdatedBy(userId);
        conversation.setUpdatedTime(new Date());
        conversationService.updateById(conversation);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("消息序列化失败", e);
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
