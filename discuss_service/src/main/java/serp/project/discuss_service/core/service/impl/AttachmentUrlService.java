/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Service implementation for attachment URL enrichment
 */

package serp.project.discuss_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.core.domain.vo.StorageLocation;
import serp.project.discuss_service.core.port.client.IStoragePort;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussCacheService.CachedAttachmentUrl;
import serp.project.discuss_service.kernel.property.StorageProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for enriching attachments with presigned URLs.
 * Generates presigned download URLs for S3/MinIO stored files with Redis caching.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentUrlService implements IAttachmentUrlService {

    private final IStoragePort storagePort;
    private final StorageProperties storageProperties;
    private final IDiscussCacheService cacheService;

    @Override
    public AttachmentResponse enrichWithUrls(AttachmentEntity attachment) {
        if (attachment == null) {
            return null;
        }

        AttachmentResponse response = AttachmentResponse.fromEntity(attachment);

        // Only generate presigned URLs if attachment has storage info
        if (attachment.getStorageKey() != null) {
            // Try cache first
            Optional<CachedAttachmentUrl> cached = cacheService.getCachedAttachmentUrl(attachment.getId());

            if (cached.isPresent() && cached.get().expiresAt() > System.currentTimeMillis()) {
                // Use cached URLs
                applyCachedUrls(response, cached.get());
                log.debug("Using cached URL for attachment {}", attachment.getId());
            } else {
                // Generate and cache new URLs
                generateAndCacheUrl(attachment, response);
            }
        }

        return response;
    }

    @Override
    public List<AttachmentResponse> enrichWithUrls(List<AttachmentEntity> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return new ArrayList<>();
        }

        return attachments.stream()
                .map(this::enrichWithUrls)
                .toList();
    }

    @Override
    public MessageResponse enrichMessageWithUrls(MessageEntity message) {
        if (message == null) {
            return null;
        }

        MessageResponse response = MessageResponse.fromEntity(message);

        // Enrich attachments with presigned URLs
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            List<AttachmentResponse> enrichedAttachments = enrichWithUrls(message.getAttachments());
            response.setAttachments(enrichedAttachments);
        }

        return response;
    }

    @Override
    public List<MessageResponse> enrichMessagesWithUrls(List<MessageEntity> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        return messages.stream()
                .map(this::enrichMessageWithUrls)
                .toList();
    }

    @Override
    public int getUrlExpiryDays() {
        return storageProperties.getS3().getDownloadUrlExpiryDays();
    }

    /**
     * Apply cached URL information to the response
     */
    private void applyCachedUrls(AttachmentResponse response, CachedAttachmentUrl cached) {
        response.setDownloadUrl(cached.downloadUrl());
        response.setThumbnailUrl(cached.thumbnailUrl());
        response.setUrlExpiresAt(cached.expiresAt());
    }

    /**
     * Generate a new presigned URL and cache it
     */
    private void generateAndCacheUrl(AttachmentEntity attachment, AttachmentResponse response) {
        try {
            StorageLocation location = StorageLocation.ofS3(
                    attachment.getStorageBucket(),
                    attachment.getStorageKey()
            );

            Duration expiry = Duration.ofDays(getUrlExpiryDays());
            String presignedUrl = storagePort.generatePresignedUrl(location, expiry);
            long expiresAt = calculateExpiryTimestamp(expiry);

            // Set download URL
            response.setDownloadUrl(presignedUrl);

            // For images/videos, thumbnailUrl is the same as downloadUrl
            String thumbnailUrl = null;
            if (attachment.isImage() || attachment.isVideo()) {
                thumbnailUrl = presignedUrl;
                response.setThumbnailUrl(thumbnailUrl);
            }

            // Set expiry timestamp
            response.setUrlExpiresAt(expiresAt);

            // Cache the URL
            CachedAttachmentUrl urlInfo = new CachedAttachmentUrl(presignedUrl, thumbnailUrl, expiresAt);
            cacheService.cacheAttachmentUrl(attachment.getId(), urlInfo);

            log.debug("Generated and cached presigned URL for attachment {}: expires at {}",
                    attachment.getId(), expiresAt);

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for attachment {}: {}",
                    attachment.getId(), e.getMessage());
            // Leave URLs as null if generation fails
        }
    }

    /**
     * Calculate the expiry timestamp based on the duration
     *
     * @param expiry Duration until expiry
     * @return Unix timestamp in milliseconds when the URL expires
     */
    private long calculateExpiryTimestamp(Duration expiry) {
        return Instant.now().plus(expiry).toEpochMilli();
    }
}
