package com.clj.ai.controller;

import com.clj.ai.service.MinioService;
import com.clj.common.exception.BusinessException;
import com.clj.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 聊天附件上传控制器
 * <p>
 * 附件只做文件存储，不写入知识库、不做向量化：
 * - 图片 → {bucket}/images/uuid_文件名，前端与模型按图片处理
 * - 其它文件（pdf、word、excel 等）→ {bucket}/upload/uuid_文件名，由模型读取内容
 * 返回的地址为公网域名形式（http://img.ajie-ai.xyz/...），会随消息一起入库。
 */
@RestController
@RequestMapping("/ai/file")
@RequiredArgsConstructor
public class FileController {

    private static final String IMAGE_FOLDER = "images";
    private static final String FILE_FOLDER = "upload";

    private static final String IMAGE_PATTERN = ".*\\.(jpg|jpeg|png|gif|webp|bmp)$";
    private static final String DOCUMENT_PATTERN = ".*\\.(pdf|doc|docx|txt|md|xlsx|xls|ppt|pptx|csv)$";

    private final MinioService minioService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > 10L * 1024 * 1024) {
            throw new BusinessException("单个附件不能超过10MB");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        boolean image = name.matches(IMAGE_PATTERN);
        boolean document = name.matches(DOCUMENT_PATTERN);
        if (!image && !document) {
            throw new BusinessException("不支持的附件类型");
        }
        return Result.success(minioService.upload(file, image ? IMAGE_FOLDER : FILE_FOLDER));
    }
}
