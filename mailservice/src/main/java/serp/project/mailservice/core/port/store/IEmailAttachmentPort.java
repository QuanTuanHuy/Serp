/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.store;

import serp.project.mailservice.core.domain.entity.EmailAttachmentEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IEmailAttachmentPort {
    EmailAttachmentEntity save(EmailAttachmentEntity attachment);

    Optional<EmailAttachmentEntity> findById(Long id);

    List<EmailAttachmentEntity> findByEmailId(Long emailId);

    List<EmailAttachmentEntity> findExpiredAttachments(LocalDateTime before);

    void delete(Long id);

    void deleteByEmailId(Long emailId);

    long getTotalSizeByEmailId(Long emailId);
}
