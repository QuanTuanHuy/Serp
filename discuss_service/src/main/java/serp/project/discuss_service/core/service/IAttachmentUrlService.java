/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Service interface for attachment URL enrichment
 */

package serp.project.discuss_service.core.service;

import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.domain.dto.response.MessageResponse;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.domain.entity.MessageEntity;

import java.util.List;

/**
 * Service interface for enriching attachments with presigned URLs.
 * Generates presigned download URLs for S3/MinIO stored files.
 */
public interface IAttachmentUrlService {

    /**
     * Enrich a single attachment with presigned download URL
     *
     * @param attachment The attachment entity
     * @return AttachmentResponse with downloadUrl and thumbnailUrl populated
     */
    AttachmentResponse enrichWithUrls(AttachmentEntity attachment);

    /**
     * Enrich multiple attachments with presigned download URLs
     *
     * @param attachments List of attachment entities
     * @return List of AttachmentResponses with URLs populated
     */
    List<AttachmentResponse> enrichWithUrls(List<AttachmentEntity> attachments);

    /**
     * Enrich a message response with presigned URLs for all its attachments
     *
     * @param message The message entity
     * @return MessageResponse with enriched attachments
     */
    MessageResponse enrichMessageWithUrls(MessageEntity message);

    /**
     * Enrich multiple messages with presigned URLs for their attachments
     *
     * @param messages List of message entities
     * @return List of MessageResponses with enriched attachments
     */
    List<MessageResponse> enrichMessagesWithUrls(List<MessageEntity> messages);

    /**
     * Get the configured URL expiry duration in days
     *
     * @return Number of days until URLs expire
     */
    int getUrlExpiryDays();
}
