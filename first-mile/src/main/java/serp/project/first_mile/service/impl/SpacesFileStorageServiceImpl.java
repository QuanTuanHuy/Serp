/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import serp.project.first_mile.dto.request.FileUploadRequest;
import serp.project.first_mile.dto.response.FileUploadResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.service.FileStorageService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SpacesFileStorageServiceImpl implements FileStorageService {

    private static final Pattern UNSAFE_SEGMENT_PATTERN = Pattern.compile("[^a-zA-Z0-9_-]");
    private static final Pattern UNSAFE_FILE_NAME_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final String DEFAULT_SERVICE = "first-mile";
    private static final String DEFAULT_FOLDER = "files";
    private static final String DEFAULT_FILE_NAME = "upload.bin";
    private static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_OCTET_STREAM_VALUE;

    @Value("${storage.s3.endpoint:https://sgp1.digitaloceanspaces.com}")
    private String endpoint;

    @Value("${storage.s3.region:sgp1}")
    private String region;

    @Value("${storage.s3.bucket:datn-tms}")
    private String bucket;

    @Value("${storage.s3.access-key:}")
    private String accessKey;

    @Value("${storage.s3.secret-key:}")
    private String secretKey;

    @Value("${storage.s3.public-base-url:https://datn-tms.sgp1.digitaloceanspaces.com}")
    private String publicBaseUrl;

    @Value("${storage.s3.path-style-access-enabled:false}")
    private boolean pathStyleAccessEnabled;

    @Value("${storage.s3.max-file-size-bytes:104857600}")
    private long maxFileSizeBytes;

    @Override
    public FileUploadResponse upload(FileUploadRequest request) {
        validateRequest(request);
        validateConfiguration();

        String serviceSegment = sanitizeSegment(request.getServiceName(), DEFAULT_SERVICE);
        String folderSegment = sanitizeSegment(request.getFolder(), DEFAULT_FOLDER);
        String fileName = sanitizeFileName(request.getOriginalFileName());
        String contentType = Optional.ofNullable(request.getContentType())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElse(DEFAULT_CONTENT_TYPE);

        String tenantSegment = request.getTenantId() == null
                ? "tenant-unknown"
                : "tenant-" + request.getTenantId();

        String datePath = LocalDate.now(ZoneOffset.UTC).format(DATE_PATH_FORMATTER);
        String objectKey = String.join(
                "/",
                serviceSegment,
                tenantSegment,
                folderSegment,
                datePath,
                UUID.randomUUID() + "-" + fileName
        );

        Map<String, String> metadata = buildMetadata(request, serviceSegment, folderSegment);

        PutObjectRequest.Builder putObjectRequestBuilder = PutObjectRequest.builder()
                .bucket(bucket.trim())
                .key(objectKey)
                .contentType(contentType)
                .contentLength((long) request.getContent().length)
                .metadata(metadata);

        if (request.isPublicFile()) {
            putObjectRequestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
        }

        try (S3Client s3Client = buildS3Client()) {
            s3Client.putObject(putObjectRequestBuilder.build(), RequestBody.fromBytes(request.getContent()));

            return FileUploadResponse.builder()
                    .bucket(bucket.trim())
                    .objectKey(objectKey)
                    .fileName(fileName)
                    .contentType(contentType)
                    .size((long) request.getContent().length)
                    .url(buildPublicUrl(objectKey))
                    .build();
        } catch (Exception exception) {
            log.error("Upload file to Spaces failed. key={}, bucket={}", objectKey, bucket, exception);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private S3Client buildS3Client() {
        String normalizedEndpoint = normalizeEndpoint(endpoint, bucket);

        return S3Client.builder()
                .endpointOverride(URI.create(normalizedEndpoint))
                .region(Region.of(region.trim()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyleAccessEnabled)
                        .build())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())
                ))
                .build();
    }

    private String normalizeEndpoint(String rawEndpoint, String bucketName) {
        String endpointValue = Optional.ofNullable(rawEndpoint)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElse("https://sgp1.digitaloceanspaces.com");

        URI endpointUri = URI.create(endpointValue);
        String host = endpointUri.getHost();
        if (host == null || bucketName == null || bucketName.isBlank()) {
            return endpointValue;
        }

        String bucketPrefix = bucketName.trim().toLowerCase(Locale.ROOT) + ".";
        String hostLowerCase = host.toLowerCase(Locale.ROOT);
        if (!hostLowerCase.startsWith(bucketPrefix)) {
            return endpointValue;
        }

        String rootHost = host.substring(bucketPrefix.length());
        String scheme = endpointUri.getScheme() == null ? "https" : endpointUri.getScheme();
        String port = endpointUri.getPort() > 0 ? ":" + endpointUri.getPort() : "";
        return scheme + "://" + rootHost + port;
    }

    private String buildPublicUrl(String objectKey) {
        String baseUrl = Optional.ofNullable(publicBaseUrl)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElse("https://" + bucket.trim() + "." + URI.create(normalizeEndpoint(endpoint, bucket)).getHost());

        if (baseUrl.endsWith("/")) {
            return baseUrl + objectKey;
        }
        return baseUrl + "/" + objectKey;
    }

    private Map<String, String> buildMetadata(FileUploadRequest request, String serviceSegment, String folderSegment) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("service", serviceSegment);
        metadata.put("folder", folderSegment);

        if (request.getTenantId() != null) {
            metadata.put("tenant-id", String.valueOf(request.getTenantId()));
        }

        if (request.getUploaderId() != null) {
            metadata.put("uploader-id", String.valueOf(request.getUploaderId()));
        }

        return metadata;
    }

    private void validateRequest(FileUploadRequest request) {
        if (request == null || request.getContent() == null || request.getContent().length == 0) {
            throw new AppException(ErrorCode.FILE_UPLOAD_EMPTY);
        }

        if (request.getContent().length > maxFileSizeBytes) {
            throw new AppException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateConfiguration() {
        if (isBlank(accessKey) || isBlank(secretKey) || isBlank(bucket) || isBlank(endpoint) || isBlank(region)) {
            log.error("Invalid Spaces configuration. endpoint={}, region={}, bucket={}, accessKeyPresent={}, secretKeyPresent={}",
                    endpoint,
                    region,
                    bucket,
                    !isBlank(accessKey),
                    !isBlank(secretKey));
            throw new AppException(ErrorCode.STORAGE_CONFIGURATION_INVALID);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String sanitizeSegment(String value, String fallback) {
        String normalized = Optional.ofNullable(value)
                .map(String::trim)
                .orElse("")
                .replace("\\", "-")
                .replace("/", "-");

        normalized = UNSAFE_SEGMENT_PATTERN.matcher(normalized).replaceAll("-");
        normalized = normalized.replaceAll("-{2,}", "-");
        normalized = normalized.replaceAll("^-+", "");
        normalized = normalized.replaceAll("-+$", "");

        if (normalized.isBlank()) {
            return fallback;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String sanitizeFileName(String fileName) {
        String normalized = Optional.ofNullable(fileName)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElse(DEFAULT_FILE_NAME)
                .replace("\\", "/");

        int separatorIndex = normalized.lastIndexOf('/');
        if (separatorIndex >= 0 && separatorIndex < normalized.length() - 1) {
            normalized = normalized.substring(separatorIndex + 1);
        }

        normalized = UNSAFE_FILE_NAME_PATTERN.matcher(normalized).replaceAll("-");
        normalized = normalized.replaceAll("-{2,}", "-");
        normalized = normalized.replaceAll("^[.-]+", "");
        normalized = normalized.replaceAll("[.-]+$", "");

        if (normalized.isBlank()) {
            return DEFAULT_FILE_NAME;
        }
        return normalized;
    }
}
