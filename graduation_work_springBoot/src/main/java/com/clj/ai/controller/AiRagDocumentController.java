package com.clj.ai.controller;

import com.clj.ai.service.AiRagDocumentService;
import com.clj.ai.vo.DocumentUploadResultVo;
import com.clj.common.exception.BusinessException;
import com.clj.common.result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clj.ai.domain.AiRagDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * RAG 文档管理控制器
 */
@RestController
@RequestMapping("/ai/rag/document")
@RequiredArgsConstructor
public class AiRagDocumentController {

    private final AiRagDocumentService documentService;

    /**
     * 上传文档
     *
     * @param file            文件
     * @param knowledgeBaseId 知识库ID（雪花 ID，前端以字符串传输）
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result<DocumentUploadResultVo> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("knowledgeBaseId") String knowledgeBaseId) {

        if (!StringUtils.hasText(knowledgeBaseId)) {
            throw new BusinessException("知识库ID不能为空");
        }
        Long id;
        try {
            // 雪花 ID 超出 JS 安全整数范围，前端按字符串传递，这里显式解析并给出可读报错
            id = Long.valueOf(knowledgeBaseId.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("知识库ID不合法: " + knowledgeBaseId);
        }
        return Result.success(documentService.uploadDocument(file, id));
    }

    /**
     * 删除文档（逻辑删除）
     *
     * @param documentId 文档ID
     * @return 删除结果
     */
    @DeleteMapping("/{documentId}")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        documentService.deleteDocument(documentId);
        return Result.success();
    }

    /**
     * 重新索引文档
     *
     * @param documentId 文档ID
     * @return 重新索引结果
     */
    @PostMapping("/{documentId}/reindex")
    public Result<Void> reindexDocument(@PathVariable Long documentId) {
        documentService.reindexDocument(documentId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<Page<AiRagDocument>> list(
            @RequestParam Long knowledgeBaseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        return Result.success(documentService.listDocuments(knowledgeBaseId, keyword, pageNum, pageSize));
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody AiRagDocument document) {
        documentService.updateDocumentName(document.getId(), document.getName());
        return Result.success();
    }
}
