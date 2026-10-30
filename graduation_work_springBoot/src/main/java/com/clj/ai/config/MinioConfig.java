package com.clj.ai.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        String endpoint = StringUtils.hasText(properties.getEndpoint())
                ? properties.getEndpoint().trim()
                : "http://localhost:9000";
        String accessKey = StringUtils.hasText(properties.getAccessKey())
                ? properties.getAccessKey().trim()
                : "admin";
        String secretKey = StringUtils.hasText(properties.getSecretKey())
                ? properties.getSecretKey().trim()
                : "minio_password";

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(
                        accessKey,
                        secretKey
                )
                .build();
    }
}
