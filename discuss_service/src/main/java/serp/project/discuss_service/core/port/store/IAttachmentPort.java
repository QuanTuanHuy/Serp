/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment port interface
 */

package serp.project.discuss_service.core.port.store;

import serp.project.discuss_service.core.domain.entity.AttachmentEntity;

import java.util.List;
import java.util.Optional;

public interface IAttachmentPort {

    /**
     * Save an attachment (create or update)
     */
    AttachmentEntity save(AttachmentEntity attachment);

    /**
     * Save multiple attachments
     */
    List<AttachmentEntity> saveAll(List<AttachmentEntity> attachments);

    /**
     * Find attachment by ID
     */
    Optional<AttachmentEntity> findById(Long id);

    /**
     * Find attachments by message ID
     */
    List<AttachmentEntity> findByMessageId(Long messageId);

    /**
     * Find attachments by message IDs
     */
    List<AttachmentEntity> findByMessageIds(List<Long> messageIds);

    /**
     * Find attachments by channel ID
     */
    List<AttachmentEntity> findByChannelId(Long channelId);

    /**
     * Count attachments by channel
     */
    long countByChannelId(Long channelId);

    /**
     * Delete attachment by ID
     */
    void deleteById(Long id);

    /**
     * Delete attachments by message ID
     */
    void deleteByMessageId(Long messageId);
}
