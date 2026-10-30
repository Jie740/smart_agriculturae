package com.clj.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint = "http://localhost:9000";

    private String accessKey = "admin";

    private String secretKey = "minio_password_123";

    private String bucketName = "agriculture";

    /**
     * 对象访问用的公网域名（服务器已把 img.ajie-ai.xyz 绑定到 MinIO 的 9000 端口）。
     * 图片走该域名 + /{bucket}/images/xxx，文件走该域名 + /{bucket}/upload/xxx，
     * 模型和浏览器都能直接访问；数据库里存的就是这种地址。
     * 留空时回退为内网 endpoint，方便本地调试。
     */
    private String publicBaseUrl;
}
