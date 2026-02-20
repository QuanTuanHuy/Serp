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

public interface IAttachmentUrlService {

    AttachmentResponse enrichWithUrls(AttachmentEntity attachment);

    List<AttachmentResponse> enrichWithUrls(List<AttachmentEntity> attachments);

    MessageResponse enrichMessageWithUrls(MessageEntity message);

    List<MessageResponse> enrichMessagesWithUrls(List<MessageEntity> messages);

    int getUrlExpiryDays();
}
