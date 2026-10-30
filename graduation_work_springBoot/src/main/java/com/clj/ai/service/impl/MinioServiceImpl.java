package com.clj.ai.service.impl;

import com.clj.ai.config.MinioProperties;
import com.clj.ai.service.MinioService;
import com.clj.common.exception.BusinessException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final MinioClient minioClient;

    private final MinioProperties properties;

    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > 10L * 1024 * 1024) {
            throw new BusinessException("单个附件不能超过10MB");
        }
        try {
            String dir = folder.endsWith("/") ? folder : folder + "/";
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(dir + fileName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            String url = publicUrl(dir + fileName);
            log.info("上传附件成功: {}", url);
            return url;

        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public String uploadRagDocument(MultipartFile file, String objectName) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .stream(
                                    file.getInputStream(),
                                    file.getSize(),
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            return publicUrl(objectName);

        } catch (Exception e) {
            throw new BusinessException("上传 RAG 文档失败: " + e.getMessage());
        }
    }

    /**
     * 生成对象的外部访问地址：优先使用配置的公网域名（img.ajie-ai.xyz），
     * 未配置时回退为内网 endpoint，保证本地/测试环境仍可用。
     */
    private String publicUrl(String objectName) {
        String base = properties.getPublicBaseUrl();
        if (base == null || base.isBlank()) {
            base = properties.getEndpoint();
        }
        return stripTrailingSlash(base.trim()) + "/" + properties.getBucketName() + "/" + objectName;
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @Override
    public InputStream readRagDocument(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException("读取 RAG 文档失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream readByUrl(String url) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(parseObjectName(url))
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteRagDocument(String objectName) {
        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException("删除 RAG 文档失败: " + e.getMessage());
        }
    }

    @Override
    public String readImageAsDataUrl(String imageUrl) {
        try {
            // 1. 解析 URL，提取对象名（去掉 endpoint 和 bucketName 前缀）
            String objectName = parseObjectName(imageUrl);

            // 2. 获取图片 contentType（上传时保存的），读取失败时根据扩展名推断
            String contentType = getContentType(objectName);

            // 3. 从 MinIO 读取字节流
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(properties.getBucketName())
                    .object(objectName)
                    .build();

            try (InputStream inputStream = minioClient.getObject(args)) {
                byte[] bytes = inputStream.readAllBytes();

                // 4. 转 Base64 data URL（仅用于 Java → Qwen，不返回前端）
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String dataUrl = "data:" + contentType + ";base64," + base64;

                log.info("MinIO 图片已转为 data URL: object={}, size={}KB",
                        objectName, bytes.length / 1024);
                return dataUrl;
            }

        } catch (Exception e) {
            throw new BusinessException("读取MinIO图片失败: " + e.getMessage());
        }
    }

    /**
     * 从完整 URL 中解析 MinIO 对象名
     * 如 http://192.168.127.128:9000/agriculture/uuid_abc.png → uuid_abc.png
     */
    private String parseObjectName(String imageUrl) {
        String path = URI.create(imageUrl).getPath();
        String prefix = "/" + properties.getBucketName() + "/";
        if (path.startsWith(prefix)) {
            return URLDecoder.decode(path.substring(prefix.length()), StandardCharsets.UTF_8);
        }
        return URLDecoder.decode(path.startsWith("/") ? path.substring(1) : path, StandardCharsets.UTF_8);
    }

    /**
     * 获取图片 contentType：优先用 MinIO 里保存的，读取失败按扩展名推断
     */
    private String getContentType(String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getBucketName())
                            .object(objectName)
                            .build());
            if (stat.contentType() != null && !stat.contentType().isBlank()) {
                return stat.contentType();
            }
        } catch (Exception e) {
            log.warn("获取对象 contentType 失败，改用扩展名推断: object={}", objectName);
        }

        String lower = objectName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
