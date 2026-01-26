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

    List<AttachmentModel> findByMessageId(Long messageId);

    List<AttachmentModel> findByChannelId(Long channelId);

    List<AttachmentModel> findByMessageIdIn(List<Long> messageIds);

    long countByChannelId(Long channelId);

    void deleteByMessageId(Long messageId);
}
