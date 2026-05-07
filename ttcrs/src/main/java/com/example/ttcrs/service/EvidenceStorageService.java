package com.example.ttcrs.service;

import com.example.ttcrs.config.StorageProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceStorageService {

    private static final DateTimeFormatter PATH_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final S3Client ttcrsS3Client;
    private final StorageProperties storageProperties;

    @PostConstruct
    public void init() {
        String bucket = storageProperties.getS3().getBucket();
        try {
            ttcrsS3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            ttcrsS3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            log.info("Created MinIO bucket: {}", bucket);
        } catch (Exception e) {
            log.warn("Could not verify bucket {}: {}", bucket, e.getMessage());
        }
        applyPublicReadPolicy(bucket);
    }

    /** Allow anonymous GetObject so evidence URLs are viewable in a browser without auth. */
    private void applyPublicReadPolicy(String bucket) {
        String policy = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": {"AWS": ["*"]},
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """.formatted(bucket);
        try {
            ttcrsS3Client.putBucketPolicy(PutBucketPolicyRequest.builder()
                    .bucket(bucket).policy(policy).build());
            log.info("Public-read policy applied to bucket: {}", bucket);
        } catch (Exception e) {
            log.warn("Could not apply public-read policy to {}: {}", bucket, e.getMessage());
        }
    }

    public String upload(MultipartFile file, Long tenantId) {
        String bucket = storageProperties.getS3().getBucket();
        String key = buildKey(tenantId, file.getOriginalFilename());

        try {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();
            ttcrsS3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        String url = String.format("%s/%s/%s",
                storageProperties.getS3().getEndpoint(), bucket, key);
        log.info("Evidence uploaded: {}", url);
        return url;
    }

    private String buildKey(Long tenantId, String originalName) {
        String datePath = LocalDateTime.now().format(PATH_FORMAT);
        String safeName = originalName != null
                ? originalName.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "evidence";
        return String.format("tenant-%d/evidence/%s/%s_%s", tenantId, datePath, UUID.randomUUID(), safeName);
    }
}
