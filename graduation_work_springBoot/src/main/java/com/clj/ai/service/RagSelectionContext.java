package com.clj.ai.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 保存当前聊天请求的 RAG 范围。使用可继承 ThreadLocal 兼容流式模型创建的工作线程。
 */
public final class RagSelectionContext {
    private static final Map<String, Selection> CURRENT = new ConcurrentHashMap<>();

    private RagSelectionContext() {
    }

    public static void set(String key, List<Long> knowledgeBaseIds, List<Long> documentIds) {
        if (key == null || key.isBlank()) {
            return;
        }
        CURRENT.put(key, new Selection(copy(knowledgeBaseIds), copy(documentIds)));
    }

    public static Selection get(String key) {
        return key == null ? null : CURRENT.get(key);
    }

    public static void clear(String key) {
        if (key != null) {
            CURRENT.remove(key);
        }
    }

    private static List<Long> copy(List<Long> ids) {
        return ids == null ? List.of() : ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
    }

    public record Selection(List<Long> knowledgeBaseIds, List<Long> documentIds) {
    }
}
