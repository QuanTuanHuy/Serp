/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment use case
 */

package serp.project.discuss_service.core.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import serp.project.discuss_service.core.domain.dto.response.AttachmentResponse;
import serp.project.discuss_service.core.domain.dto.response.PaginatedResponse;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.exception.AppException;
import serp.project.discuss_service.core.exception.ErrorCode;
import serp.project.discuss_service.core.service.IAttachmentService;
import serp.project.discuss_service.core.service.IAttachmentUrlService;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IDiscussCacheService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttachmentUseCase {

    private final IAttachmentService attachmentService;
    private final IAttachmentUrlService attachmentUrlService;
    private final IDiscussCacheService cacheService;
    private final IChannelMemberService channelMemberService;

    @Transactional
    public AttachmentResponse uploadAttachment(Long channelId, Long messageId,
                                               MultipartFile file, Long tenantId) {
        AttachmentEntity attachment = attachmentService.uploadAttachment(file, messageId, channelId, tenantId);
        log.info("Uploaded attachment {} for message {} in channel {}", attachment.getId(), messageId, channelId);
        return attachmentUrlService.enrichWithUrls(attachment);
    }

    @Transactional
    public List<AttachmentResponse> uploadAttachments(Long channelId, Long messageId,
                                                      List<MultipartFile> files, Long tenantId) {
        List<AttachmentEntity> attachments = attachmentService.uploadAttachments(files, messageId, channelId, tenantId);
        log.info("Uploaded {} attachments for message {} in channel {}", attachments.size(), messageId, channelId);
        return attachmentUrlService.enrichWithUrls(attachments);
    }

    public AttachmentResponse getAttachment(Long attachmentId, Long tenantId) {
        AttachmentEntity attachment = attachmentService.getAttachment(attachmentId, tenantId);
        return attachmentUrlService.enrichWithUrls(attachment);
    }

    public List<AttachmentResponse> getAttachmentsByMessage(Long messageId, Long tenantId) {
        List<AttachmentEntity> attachments = attachmentService.getAttachmentsByMessage(messageId, tenantId);
        return attachmentUrlService.enrichWithUrls(attachments);
    }

    public PaginatedResponse<AttachmentResponse> getAttachmentsByChannel(
            Long channelId,
            Long userId,
            Long tenantId,
            Integer page,
            Integer pageSize) {
        if (!channelMemberService.isMember(channelId, userId)) {
            throw new AppException(ErrorCode.NOT_CHANNEL_MEMBER);
        }

        int safePage = page == null || page < 0 ? 0 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        PageRequest pageable = PageRequest.of(safePage, safePageSize);

        Page<AttachmentEntity> attachments = attachmentService.getAttachmentsByChannel(
                channelId,
                tenantId,
                pageable
        );

        List<AttachmentResponse> responses = attachments.getContent().stream()
                .map(AttachmentResponse::fromEntity)
                .toList();

        return PaginatedResponse.of(
                responses,
                attachments.getNumber(),
                attachments.getSize(),
                attachments.getTotalElements()
        );
    }

    public String getDownloadUrl(Long attachmentId, Long tenantId, int expirationMinutes) {
        return attachmentService.generateDownloadUrl(attachmentId, tenantId, expirationMinutes);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId, Long tenantId, Long userId) {
        attachmentService.deleteAttachment(attachmentId, tenantId, userId);

        cacheService.invalidateAttachmentUrl(attachmentId);

        log.info("User {} deleted attachment {} from tenant {}", userId, attachmentId, tenantId);
    }

    @Transactional
    public void deleteAttachmentsByMessage(Long messageId, Long tenantId) {
        List<AttachmentEntity> attachments = attachmentService.getAttachmentsByMessage(messageId, tenantId);

        attachmentService.deleteAttachmentsByMessage(messageId, tenantId);
        
        attachments.forEach(att -> cacheService.invalidateAttachmentUrl(att.getId()));

        log.info("Deleted {} attachments for message {}", attachments.size(), messageId);
    }

    public boolean isAllowedContentType(String contentType) {
        return attachmentService.isAllowedContentType(contentType);
    }

    public boolean isFileSizeAllowed(long fileSize) {
        return attachmentService.isFileSizeAllowed(fileSize);
    }
}
