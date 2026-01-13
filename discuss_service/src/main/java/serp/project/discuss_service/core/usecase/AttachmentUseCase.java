/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment use case
 */

package serp.project.discuss_service.core.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.service.IAttachmentService;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IDiscussCacheService;

import java.util.List;

/**
 * Use case for attachment operations.
 * Orchestrates attachment-related business logic across multiple services.
 * Handles URL enrichment and cache invalidation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AttachmentUseCase {

    private final IAttachmentService attachmentService;
    private final IAttachmentUrlService attachmentUrlService;
    private final IDiscussCacheService cacheService;

    // ==================== UPLOAD OPERATIONS ====================

    /**
     * Upload a single attachment for a message
     *
     * @param channelId The channel ID
     * @param messageId The message ID
     * @param file      The file to upload
     * @param tenantId  The tenant ID
     * @return Attachment response with enriched URLs
     */
    @Transactional
    public AttachmentResponse uploadAttachment(Long channelId, Long messageId,
                                               MultipartFile file, Long tenantId) {
        AttachmentEntity attachment = attachmentService.uploadAttachment(file, messageId, channelId, tenantId);
        log.info("Uploaded attachment {} for message {} in channel {}", attachment.getId(), messageId, channelId);
        return attachmentUrlService.enrichWithUrls(attachment);
    }

    /**
     * Upload multiple attachments for a message
     *
     * @param channelId The channel ID
     * @param messageId The message ID
     * @param files     List of files to upload
     * @param tenantId  The tenant ID
     * @return List of attachment responses with enriched URLs
     */
    @Transactional
    public List<AttachmentResponse> uploadAttachments(Long channelId, Long messageId,
                                                      List<MultipartFile> files, Long tenantId) {
        List<AttachmentEntity> attachments = attachmentService.uploadAttachments(files, messageId, channelId, tenantId);
        log.info("Uploaded {} attachments for message {} in channel {}", attachments.size(), messageId, channelId);
        return attachmentUrlService.enrichWithUrls(attachments);
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * Get attachment by ID with enriched URLs
     *
     * @param attachmentId The attachment ID
     * @param tenantId     The tenant ID
     * @return Attachment response with enriched URLs
     */
    public AttachmentResponse getAttachment(Long attachmentId, Long tenantId) {
        AttachmentEntity attachment = attachmentService.getAttachment(attachmentId, tenantId);
        return attachmentUrlService.enrichWithUrls(attachment);
    }

    /**
     * Get all attachments for a message with enriched URLs
     *
     * @param messageId The message ID
     * @param tenantId  The tenant ID
     * @return List of attachment responses with enriched URLs
     */
    public List<AttachmentResponse> getAttachmentsByMessage(Long messageId, Long tenantId) {
        List<AttachmentEntity> attachments = attachmentService.getAttachmentsByMessage(messageId, tenantId);
        return attachmentUrlService.enrichWithUrls(attachments);
    }

    /**
     * Generate a download URL for an attachment
     *
     * @param attachmentId      The attachment ID
     * @param tenantId          The tenant ID
     * @param expirationMinutes URL expiration in minutes
     * @return Presigned download URL
     */
    public String getDownloadUrl(Long attachmentId, Long tenantId, int expirationMinutes) {
        return attachmentService.generateDownloadUrl(attachmentId, tenantId, expirationMinutes);
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Delete an attachment with cache invalidation
     *
     * @param attachmentId The attachment ID to delete
     * @param tenantId     The tenant ID
     * @param userId       The user performing the deletion
     */
    @Transactional
    public void deleteAttachment(Long attachmentId, Long tenantId, Long userId) {
        // Delete from storage and database
        attachmentService.deleteAttachment(attachmentId, tenantId, userId);

        // Invalidate URL cache
        cacheService.invalidateAttachmentUrl(attachmentId);

        log.info("User {} deleted attachment {} from tenant {}", userId, attachmentId, tenantId);
    }

    /**
     * Delete all attachments for a message with cache invalidation
     *
     * @param messageId The message ID
     * @param tenantId  The tenant ID
     */
    @Transactional
    public void deleteAttachmentsByMessage(Long messageId, Long tenantId) {
        // Get attachments first to know which cache entries to invalidate
        List<AttachmentEntity> attachments = attachmentService.getAttachmentsByMessage(messageId, tenantId);

        // Delete from storage and database
        attachmentService.deleteAttachmentsByMessage(messageId, tenantId);

        // Invalidate cache for each attachment (one-by-one)
        attachments.forEach(att -> cacheService.invalidateAttachmentUrl(att.getId()));

        log.info("Deleted {} attachments for message {}", attachments.size(), messageId);
    }

    // ==================== VALIDATION OPERATIONS ====================

    /**
     * Check if content type is allowed for upload
     *
     * @param contentType The MIME type to check
     * @return true if allowed
     */
    public boolean isAllowedContentType(String contentType) {
        return attachmentService.isAllowedContentType(contentType);
    }

    /**
     * Check if file size is allowed for upload
     *
     * @param fileSize The file size in bytes
     * @return true if allowed
     */
    public boolean isFileSizeAllowed(long fileSize) {
        return attachmentService.isFileSizeAllowed(fileSize);
    }
}
