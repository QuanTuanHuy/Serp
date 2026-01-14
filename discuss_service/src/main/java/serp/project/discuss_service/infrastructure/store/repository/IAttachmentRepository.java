/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment repository
 */

package serp.project.discuss_service.infrastructure.store.repository;

import org.springframework.stereotype.Repository;
import serp.project.discuss_service.infrastructure.store.model.AttachmentModel;

import java.util.List;

@Repository
public interface IAttachmentRepository extends IBaseRepository<AttachmentModel> {

    // Find attachments by message
    List<AttachmentModel> findByMessageId(Long messageId);

    // Find attachments by channel
    List<AttachmentModel> findByChannelId(Long channelId);

    // Find attachments by message IDs
    List<AttachmentModel> findByMessageIdIn(List<Long> messageIds);

    // Count attachments by channel
    long countByChannelId(Long channelId);

    // Delete attachments by message
    void deleteByMessageId(Long messageId);
}
