package com.clj.ai.dto;

import lombok.Data;

@Data
public class AiKnowledgeBaseTypeDto {
    private Long id;
    private String typeName;
    private String typeCode;
    private String description;
    private Integer sortNo;
    private Integer status;
}
