package com.clj.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * RAG 检索结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagSearchResultVo {

    /**
     * 文档ID（雪花 ID，序列化为字符串避免前端精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long documentId;

    /**
     * Chunk ID（雪花 ID，序列化为字符串避免前端精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chunkId;

    /**
     * Chunk 文本内容
     */
    private String content;

    /**
     * 所属知识库ID（雪花 ID，序列化为字符串避免前端精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long knowledgeBaseId;

    /**
     * 所属知识库名称
     */
    private String knowledgeBaseName;

    /**
     * 相似度得分
     */
    private Double score;

    /**
     * 元数据（包含 file_name, chunk_index 等）
     */
    private Map<String, Object> metadata;
}
