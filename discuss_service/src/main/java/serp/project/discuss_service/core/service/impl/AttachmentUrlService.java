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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


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

        if (attachment.getStorageKey() != null) {
            Optional<CachedAttachmentUrl> cached = cacheService.getCachedAttachmentUrl(attachment.getId());

            if (cached.isPresent() && cached.get().expiresAt() > System.currentTimeMillis()) {
                applyCachedUrls(response, cached.get());
                log.debug("Using cached URL for attachment {}", attachment.getId());
            } else {
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

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<AttachmentResponse>> futures = attachments.stream()
                    .map(attachment -> executor.submit(() -> enrichWithUrls(attachment)))
                    .toList();

            return futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (Exception e) {
                            log.warn("Failed to enrich attachment with URL: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    @Override
    public MessageResponse enrichMessageWithUrls(MessageEntity message) {
        if (message == null) {
            return null;
        }

        MessageResponse response = MessageResponse.fromEntity(message);

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

    private void applyCachedUrls(AttachmentResponse response, CachedAttachmentUrl cached) {
        response.setDownloadUrl(cached.downloadUrl());
        response.setThumbnailUrl(cached.thumbnailUrl());
        response.setUrlExpiresAt(cached.expiresAt());
    }

    private void generateAndCacheUrl(AttachmentEntity attachment, AttachmentResponse response) {
        try {
            StorageLocation location = StorageLocation.ofS3(
                    attachment.getStorageBucket(),
                    attachment.getStorageKey()
            );

            Duration expiry = Duration.ofDays(getUrlExpiryDays());
            String presignedUrl = storagePort.generatePresignedUrl(location, expiry);
            long expiresAt = calculateExpiryTimestamp(expiry);

            response.setDownloadUrl(presignedUrl);

            // For images/videos, thumbnailUrl is the same as downloadUrl
            String thumbnailUrl = null;
            if (attachment.isImage() || attachment.isVideo()) {
                thumbnailUrl = presignedUrl;
                response.setThumbnailUrl(thumbnailUrl);
            }

            response.setUrlExpiresAt(expiresAt);

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

    private long calculateExpiryTimestamp(Duration expiry) {
        return Instant.now().plus(expiry).toEpochMilli();
    }
}
