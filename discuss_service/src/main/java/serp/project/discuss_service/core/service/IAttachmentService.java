/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment service interface
 */

package serp.project.discuss_service.core.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;

import java.util.List;
import java.util.Map;

public interface IAttachmentService {

    AttachmentEntity uploadAttachment(MultipartFile file, Long messageId, Long channelId, Long tenantId);

    List<AttachmentEntity> uploadAttachments(List<MultipartFile> files, Long messageId, Long channelId, Long tenantId);

    AttachmentEntity getAttachment(Long attachmentId, Long tenantId);

    List<AttachmentEntity> getAttachmentsByMessage(Long messageId, Long tenantId);

    String generateDownloadUrl(Long attachmentId, Long tenantId, int expirationMinutes);

    void deleteAttachment(Long attachmentId, Long tenantId, Long userId);

    void deleteAttachmentsByMessage(Long messageId, Long tenantId);

    Map<Long, List<AttachmentEntity>> getAttachmentsByMessageIds(List<Long> messageIds);

    boolean isAllowedContentType(String contentType);

    boolean isFileSizeAllowed(long fileSize);
}
