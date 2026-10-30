package com.clj.ai.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequestDto {

    /**
     * 用户输入文本最大字符数
     */
    public static final int MAX_TEXT_LENGTH = 2000;

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
    @Size(max = MAX_TEXT_LENGTH, message = "输入文本不能超过" + MAX_TEXT_LENGTH + "个字符")
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
