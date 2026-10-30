package com.clj.ai.service;

import dev.langchain4j.data.message.UserMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存本次请求真正要发给模型的多模态用户消息（文本 + 图片）。
 * <p>
 * LangChain4j 的声明式 AiService 无法通过方法参数传递 ImageContent，
 * 因此由 ChatController 在这里登记，AiServiceConfig 的 chatRequestTransformer
 * 在请求真正发给模型前把它替换进去。
 * <p>
 * 值与 RagSelectionContext 一样按 memoryId 隔离，取一次即失效，
 * 保证带工具调用的多轮请求只在第一轮注入图片。
 */
public final class PendingUserMessageContext {

    private static final Map<String, UserMessage> PENDING = new ConcurrentHashMap<>();

    private PendingUserMessageContext() {
    }

    public static void set(String key, UserMessage userMessage) {
        if (key == null || key.isBlank() || userMessage == null) {
            return;
        }
        PENDING.put(key, userMessage);
    }

    /**
     * 取出并移除，确保同一次请求只会被替换一次
     */
    public static UserMessage consume(String key) {
        return key == null ? null : PENDING.remove(key);
    }

    public static void clear(String key) {
        if (key != null) {
            PENDING.remove(key);
        }
    }
}
