package com.clj.ai.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinioService {

    /**
     * 上传聊天附件到指定目录
     * <p>
     * 图片放 images/，其它文件（pdf、word 等）放 upload/，
     * 返回的是公网域名形式的地址（如 http://img.ajie-ai.xyz/agriculture/upload/xxx.pdf），
     * 该地址会一并存入数据库，模型/浏览器都通过它访问文件。
     *
     * @param file   文件
     * @param folder 目录名，如 images、upload
     * @return 文件访问 URL
     */
    String upload(MultipartFile file, String folder);

    /**
     * 上传 RAG 文档到指定路径
     *
     * @param file 文件
     * @param objectName 对象路径（如 rag/1/123/document.pdf）
     * @return 文件访问 URL
     */
    String uploadRagDocument(MultipartFile file, String objectName);

    /**
     * 从 MinIO 读取 RAG 文档流
     *
     * @param objectName 对象路径
     * @return 输入流
     */
    InputStream readRagDocument(String objectName);

    /**
     * 按访问地址读取对象流（附件地址可能是公网域名，这里统一解析出对象名）
     *
     * @param url 文件的访问地址
     * @return 输入流
     */
    InputStream readByUrl(String url);

    /**
     * 删除 MinIO 中的 RAG 文档
     *
     * @param objectName 对象路径
     */
    void deleteRagDocument(String objectName);

    /**
     * 根据图片 URL 从 MinIO 读取图片，转为 Base64 data URL
     * <p>
     * 前端传的是 upload 返回的完整 URL（如 http://192.168.127.128:9000/agriculture/xxx.png），
     * 后端解析出对象名后从 MinIO 读取字节流，转换为 data:image/png;base64,xxx 格式。
     * Base64 仅在 Java → Qwen 这一段使用，不返回给前端。
     *
     * @param imageUrl MinIO 图片完整 URL
     * @return data URL（data:image/png;base64,xxxxxx）
     */
    public String readImageAsDataUrl(String imageUrl);
}
