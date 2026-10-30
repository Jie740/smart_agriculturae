package com.clj.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequestDto {
    /**
     * 模型名称
     */
    private String model;
    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 编辑用户消息后重新生成时，替换的原消息ID。
     */
    private Long replaceMessageId;

    /**
     * 用户输入文本
     */
    private String text;

    /**
     * 图片URL（可选）
     */
    private List<String> imageUrls;

    private List<ChatAttachmentDto> attachments;

    private List<Long> knowledgeBaseIds;

    private List<Long> documentIds;

    private String ragScopeKey;
}
