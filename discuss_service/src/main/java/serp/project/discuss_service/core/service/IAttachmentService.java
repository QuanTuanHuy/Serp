/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment service interface
 */

package serp.project.discuss_service.core.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.vo.FileUploadResult;

import java.util.List;

/**
 * Service interface for attachment operations.
 * Handles file upload, download, and management.
 */
public interface IAttachmentService {

    /**
     * Upload a file and create an attachment record
     *
     * @param file       The file to upload
     * @param messageId  The message ID this attachment belongs to
     * @param channelId  The channel ID
     * @param tenantId   The tenant ID
     * @return The created attachment entity with storage info
     */
    AttachmentEntity uploadAttachment(MultipartFile file, Long messageId, Long channelId, Long tenantId);

    /**
     * Upload multiple files for a message
     *
     * @param files      List of files to upload
     * @param messageId  The message ID
     * @param channelId  The channel ID
     * @param tenantId   The tenant ID
     * @return List of created attachment entities
     */
    List<AttachmentEntity> uploadAttachments(List<MultipartFile> files, Long messageId, Long channelId, Long tenantId);

    /**
     * Get attachment by ID
     *
     * @param attachmentId The attachment ID
     * @param tenantId     The tenant ID for security check
     * @return The attachment entity or null if not found
     */
    AttachmentEntity getAttachment(Long attachmentId, Long tenantId);

    /**
     * Get all attachments for a message
     *
     * @param messageId The message ID
     * @param tenantId  The tenant ID
     * @return List of attachments
     */
    List<AttachmentEntity> getAttachmentsByMessage(Long messageId, Long tenantId);

    /**
     * Generate a presigned download URL for an attachment
     *
     * @param attachmentId The attachment ID
     * @param tenantId     The tenant ID
     * @param expirationMinutes How long the URL should be valid
     * @return The presigned URL
     */
    String generateDownloadUrl(Long attachmentId, Long tenantId, int expirationMinutes);

    /**
     * Delete an attachment (removes from storage and database)
     *
     * @param attachmentId The attachment ID
     * @param tenantId     The tenant ID
     * @param userId       The user requesting deletion (for audit)
     */
    void deleteAttachment(Long attachmentId, Long tenantId, Long userId);

    /**
     * Delete all attachments for a message
     *
     * @param messageId The message ID
     * @param tenantId  The tenant ID
     */
    void deleteAttachmentsByMessage(Long messageId, Long tenantId);

    /**
     * Check if file type is allowed for upload
     *
     * @param contentType The MIME type of the file
     * @return true if allowed, false otherwise
     */
    boolean isAllowedContentType(String contentType);

    /**
     * Check if file size is within limits
     *
     * @param fileSize The file size in bytes
     * @return true if within limits, false otherwise
     */
    boolean isFileSizeAllowed(long fileSize);
}
