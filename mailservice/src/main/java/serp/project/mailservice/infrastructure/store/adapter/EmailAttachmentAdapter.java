/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import serp.project.mailservice.core.domain.entity.EmailAttachmentEntity;
import serp.project.mailservice.core.port.store.IEmailAttachmentPort;
import serp.project.mailservice.infrastructure.store.mapper.EmailAttachmentModelMapper;
import serp.project.mailservice.infrastructure.store.repository.EmailAttachmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailAttachmentAdapter implements IEmailAttachmentPort {
    private final EmailAttachmentRepository emailAttachmentRepository;

    @Override
    public EmailAttachmentEntity save(EmailAttachmentEntity attachment) {
        var model = EmailAttachmentModelMapper.toModel(attachment);
        var savedModel = emailAttachmentRepository.save(model);
        return EmailAttachmentModelMapper.toEntity(savedModel);
    }

    @Override
    public Optional<EmailAttachmentEntity> findById(Long id) {
        return emailAttachmentRepository.findById(id)
                .map(EmailAttachmentModelMapper::toEntity);
    }

    @Override
    public List<EmailAttachmentEntity> findByEmailId(Long emailId) {
        var models = emailAttachmentRepository.findByEmailId(emailId);
        return EmailAttachmentModelMapper.toEntities(models);
    }

    @Override
    public List<EmailAttachmentEntity> findExpiredAttachments(LocalDateTime before) {
        var models = emailAttachmentRepository.findByExpiresAtBefore(before);
        return EmailAttachmentModelMapper.toEntities(models);
    }

    @Override
    public void delete(Long id) {
        emailAttachmentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByEmailId(Long emailId) {
        emailAttachmentRepository.deleteByEmailId(emailId);
    }

    @Override
    public long getTotalSizeByEmailId(Long emailId) {
        return emailAttachmentRepository.getTotalSizeByEmailId(emailId);
    }
}
