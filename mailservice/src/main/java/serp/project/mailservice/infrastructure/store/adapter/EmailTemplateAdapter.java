/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.core.domain.enums.EmailType;
import serp.project.mailservice.core.port.store.IEmailTemplatePort;
import serp.project.mailservice.infrastructure.store.mapper.EmailTemplateModelMapper;
import serp.project.mailservice.infrastructure.store.repository.EmailTemplateRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateAdapter implements IEmailTemplatePort {
    private final EmailTemplateRepository emailTemplateRepository;

    @Override
    public EmailTemplateEntity save(EmailTemplateEntity template) {
        var model = EmailTemplateModelMapper.toModel(template);
        var savedModel = emailTemplateRepository.save(model);
        return EmailTemplateModelMapper.toEntity(savedModel);
    }

    @Override
    public Optional<EmailTemplateEntity> findById(Long id) {
        return emailTemplateRepository.findById(id)
                .map(EmailTemplateModelMapper::toEntity);
    }

    @Override
    public Optional<EmailTemplateEntity> findByCode(String code) {
        return emailTemplateRepository.findByCode(code)
                .map(EmailTemplateModelMapper::toEntity);
    }

    @Override
    public Optional<EmailTemplateEntity> findByTenantIdAndCode(Long tenantId, String code) {
        return emailTemplateRepository.findByTenantIdAndCode(tenantId, code)
                .map(EmailTemplateModelMapper::toEntity);
    }

    @Override
    public List<EmailTemplateEntity> findByTenantId(Long tenantId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var models = emailTemplateRepository.findByTenantId(tenantId, pageable);
        return EmailTemplateModelMapper.toEntities(models);
    }

    @Override
    public List<EmailTemplateEntity> findGlobalTemplates() {
        var models = emailTemplateRepository.findByIsGlobalTrue();
        return EmailTemplateModelMapper.toEntities(models);
    }

    @Override
    public List<EmailTemplateEntity> findByType(EmailType type) {
        var models = emailTemplateRepository.findByType(type);
        return EmailTemplateModelMapper.toEntities(models);
    }

    @Override
    public void delete(Long id) {
        emailTemplateRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return emailTemplateRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndTenantId(String code, Long tenantId) {
        return emailTemplateRepository.existsByCodeAndTenantId(code, tenantId);
    }
}
