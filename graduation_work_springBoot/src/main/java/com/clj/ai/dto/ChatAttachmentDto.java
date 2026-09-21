package com.clj.ai.dto;

import lombok.Data;

@Data
public class ChatAttachmentDto {
    private String name;
    private String url;
    private String type;
    private Long size;
}
