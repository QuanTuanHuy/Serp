/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - S3 storage adapter implementation
 */

package serp.project.discuss_service.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.enums.StorageProvider;
import serp.project.discuss_service.core.domain.vo.FileUploadResult;
import serp.project.discuss_service.core.domain.vo.StorageLocation;
import serp.project.discuss_service.core.port.client.IStoragePort;
import serp.project.discuss_service.kernel.property.StorageProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * S3-compatible storage adapter.
 * Works with both MinIO and AWS S3.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class S3StorageAdapter implements IStoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;

    private static final DateTimeFormatter PATH_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    @PostConstruct
    public void init() {
        ensureBucketExists();
    }

    @Override
    public FileUploadResult upload(InputStream inputStream, String fileName, String contentType,
                                   Long fileSize, Long tenantId, Long channelId) {
        try {
            String key = generateStorageKey(tenantId, channelId, fileName);
            String bucket = getBucket();

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(inputStream, fileSize));

            String url = String.format("%s/%s/%s",
                    storageProperties.getS3().getEndpoint(), bucket, key);
            StorageLocation location = StorageLocation.ofS3(bucket, key, url);
            log.info("Uploaded file to S3: bucket={}, key={}", bucket, key);

            return FileUploadResult.success(location, contentType, fileSize);

        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            return FileUploadResult.failure("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(StorageLocation location) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(location.getBucket())
                    .key(location.getKey())
                    .build();

            return s3Client.getObject(getRequest);

        } catch (Exception e) {
            log.error("Failed to download file from S3: bucket={}, key={}, error={}",
                    location.getBucket(), location.getKey(), e.getMessage());
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(StorageLocation location) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(location.getBucket())
                    .key(location.getKey())
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted file from S3: bucket={}, key={}", location.getBucket(), location.getKey());
            return true;

        } catch (Exception e) {
            log.error("Failed to delete file from S3: bucket={}, key={}, error={}",
                    location.getBucket(), location.getKey(), e.getMessage());
            return false;
        }
    }

    @Override
    public String generatePresignedUrl(StorageLocation location, Duration expiry) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(location.getBucket())
                    .key(location.getKey())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiry)
                    .getObjectRequest(getRequest)
                    .build();

            String url = s3Presigner.presignGetObject(presignRequest).url().toString();
            log.debug("Generated presigned URL for: bucket={}, key={}", location.getBucket(), location.getKey());
            return url;

        } catch (Exception e) {
            log.error("Failed to generate presigned URL: bucket={}, key={}, error={}",
                    location.getBucket(), location.getKey(), e.getMessage());
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(StorageLocation location) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(location.getBucket())
                    .key(location.getKey())
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check file existence: bucket={}, key={}, error={}",
                    location.getBucket(), location.getKey(), e.getMessage());
            return false;
        }
    }

    @Override
    public StorageProvider getProvider() {
        return StorageProvider.S3;
    }

    @Override
    public String getBucket() {
        return storageProperties.getS3().getBucket();
    }

    @Override
    public void ensureBucketExists() {
        String bucket = getBucket();
        try {
            HeadBucketRequest headRequest = HeadBucketRequest.builder()
                    .bucket(bucket)
                    .build();
            s3Client.headBucket(headRequest);
            log.info("S3 bucket exists: {}", bucket);

        } catch (NoSuchBucketException e) {
            log.info("S3 bucket does not exist, creating: {}", bucket);
            try {
                CreateBucketRequest createRequest = CreateBucketRequest.builder()
                        .bucket(bucket)
                        .build();
                s3Client.createBucket(createRequest);
                log.info("Created S3 bucket: {}", bucket);

            } catch (Exception createEx) {
                log.error("Failed to create S3 bucket: {}", createEx.getMessage());
                throw new RuntimeException("Failed to create S3 bucket: " + bucket, createEx);
            }
        } catch (Exception e) {
            log.warn("Could not check bucket existence, will try to use anyway: {}", e.getMessage());
        }
    }

    /**
     * Generate a unique storage key for the file.
     * Format: tenant-{tenantId}/channel-{channelId}/{year}/{month}/{uuid}_{filename}
     */
    private String generateStorageKey(Long tenantId, Long channelId, String fileName) {
        String datePath = LocalDateTime.now().format(PATH_DATE_FORMAT);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedFileName = sanitizeFileName(fileName);

        return String.format("tenant-%d/channel-%d/%s/%s_%s",
                tenantId, channelId, datePath, uuid, sanitizedFileName);
    }

    /**
     * Sanitize file name to be safe for storage
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unnamed";
        }
        // Replace spaces and special characters
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
