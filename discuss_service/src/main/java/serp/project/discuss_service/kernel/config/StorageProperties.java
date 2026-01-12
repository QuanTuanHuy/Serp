/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Storage configuration properties
 */

package serp.project.discuss_service.kernel.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for storage (S3-compatible: MinIO/AWS S3)
 */
@Component
@ConfigurationProperties(prefix = "discuss.storage")
@Getter
@Setter
public class StorageProperties {

    /**
     * Storage provider type (S3 for MinIO/AWS S3)
     */
    private String provider = "S3";

    /**
     * S3-compatible storage configuration
     */
    private S3Properties s3 = new S3Properties();

    /**
     * File upload limits
     */
    private UploadLimits upload = new UploadLimits();

    @Getter
    @Setter
    public static class S3Properties {
        /**
         * S3 endpoint URL (e.g., http://localhost:9000 for MinIO)
         * Leave empty for AWS S3 (uses default endpoint)
         */
        private String endpoint;

        /**
         * Access key (MinIO root user or AWS access key)
         */
        private String accessKey;

        /**
         * Secret key (MinIO root password or AWS secret key)
         */
        private String secretKey;

        /**
         * Bucket name for storing attachments
         */
        private String bucket = "discuss-attachments";

        /**
         * AWS region (required for SDK, use us-east-1 for MinIO)
         */
        private String region = "us-east-1";

        /**
         * URL expiry duration for presigned URLs in minutes
         */
        private int presignedUrlExpiryMinutes = 60;
    }

    @Getter
    @Setter
    public static class UploadLimits {
        /**
         * Maximum file size in bytes (default: 50MB)
         */
        private long maxFileSize = 50 * 1024 * 1024;

        /**
         * Maximum total size per message in bytes (default: 100MB)
         */
        private long maxTotalSizePerMessage = 100 * 1024 * 1024;

        /**
         * Maximum number of files per message
         */
        private int maxFilesPerMessage = 10;

        /**
         * Allowed content types (empty = allow all)
         */
        private String[] allowedContentTypes = {
                "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "text/plain", "text/csv",
                "application/zip", "application/x-rar-compressed",
                "video/mp4", "video/webm", "video/quicktime",
                "audio/mpeg", "audio/wav", "audio/ogg"
        };
    }
}
