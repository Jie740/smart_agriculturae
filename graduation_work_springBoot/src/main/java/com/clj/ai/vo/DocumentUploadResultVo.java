package com.clj.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档上传结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResultVo {

    /**
     * 文档ID（雪花 ID，序列化为字符串避免前端精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long documentId;

    /**
     * 文档名称
     */
    private String documentName;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 处理状态：PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败
     */
    private String status;
}
