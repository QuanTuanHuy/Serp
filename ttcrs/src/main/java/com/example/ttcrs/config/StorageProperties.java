package com.example.ttcrs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private S3Properties s3 = new S3Properties();

    @Data
    public static class S3Properties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String region = "us-east-1";
        private String bucket = "ttcrs-evidence";
    }
}
