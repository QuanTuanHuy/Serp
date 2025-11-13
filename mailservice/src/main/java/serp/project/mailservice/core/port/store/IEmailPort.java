/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.store;

import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IEmailPort {
    EmailEntity save(EmailEntity email);

    Optional<EmailEntity> findById(Long id);

    Optional<EmailEntity> findByMessageId(String messageId);

    List<EmailEntity> findByTenantIdAndStatus(Long tenantId, EmailStatus status, int page, int size);

    List<EmailEntity> findEmailsForRetry(EmailStatus status, LocalDateTime beforeTime, int maxRetries, int limit);

    List<EmailEntity> findPendingEmails(int limit);

    List<EmailEntity> findByUserId(Long userId, int page, int size);

    void delete(Long id);

    void updateStatus(Long id, EmailStatus status, String errorMessage);

    long countByTenantIdAndStatus(Long tenantId, EmailStatus status);
}
