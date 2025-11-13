/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import serp.project.mailservice.core.domain.entity.EmailEntity;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.port.store.IEmailPort;
import serp.project.mailservice.infrastructure.store.mapper.EmailModelMapper;
import serp.project.mailservice.infrastructure.store.repository.EmailRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailAdapter implements IEmailPort {
    private final EmailRepository emailRepository;

    @Override
    public EmailEntity save(EmailEntity email) {
        var model = EmailModelMapper.toModel(email);
        var savedModel = emailRepository.save(model);
        return EmailModelMapper.toEntity(savedModel);
    }

    @Override
    public Optional<EmailEntity> findById(Long id) {
        return emailRepository.findById(id)
                .map(EmailModelMapper::toEntity);
    }

    @Override
    public Optional<EmailEntity> findByMessageId(String messageId) {
        return emailRepository.findByMessageId(messageId)
                .map(EmailModelMapper::toEntity);
    }

    @Override
    public List<EmailEntity> findByTenantIdAndStatus(Long tenantId, EmailStatus status, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var models = emailRepository.findByTenantIdAndStatus(tenantId, status, pageable);
        return EmailModelMapper.toEntities(models);
    }

    @Override
    public List<EmailEntity> findEmailsForRetry(EmailStatus status, LocalDateTime beforeTime, int maxRetries, int limit) {
        var pageable = PageRequest.of(0, limit);
        var models = emailRepository.findEmailsForRetry(status, beforeTime, maxRetries, pageable);
        return EmailModelMapper.toEntities(models);
    }

    @Override
    public List<EmailEntity> findPendingEmails(int limit) {
        var pageable = PageRequest.of(0, limit);
        var models = emailRepository.findPendingEmails(EmailStatus.PENDING, pageable);
        return EmailModelMapper.toEntities(models);
    }

    @Override
    public List<EmailEntity> findByUserId(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var models = emailRepository.findByUserId(userId, pageable);
        return EmailModelMapper.toEntities(models);
    }

    @Override
    public void delete(Long id) {
        emailRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, EmailStatus status, String errorMessage) {
        emailRepository.findById(id).ifPresent(model -> {
            model.setStatus(status);
            model.setErrorMessage(errorMessage);
            if (status == EmailStatus.SENT) {
                model.setSentAt(LocalDateTime.now());
            } else if (status == EmailStatus.FAILED) {
                model.setFailedAt(LocalDateTime.now());
            }
            emailRepository.save(model);
        });
    }

    @Override
    public long countByTenantIdAndStatus(Long tenantId, EmailStatus status) {
        return emailRepository.countByTenantIdAndStatus(tenantId, status);
    }
}
