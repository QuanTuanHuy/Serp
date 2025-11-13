/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.mailservice.core.domain.dto.request.EmailTemplateRequest;
import serp.project.mailservice.core.domain.dto.response.EmailTemplateResponse;
import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.core.domain.enums.ActiveStatus;
import serp.project.mailservice.core.domain.mapper.EmailTemplateMapper;
import serp.project.mailservice.core.port.client.IRedisCachePort;
import serp.project.mailservice.core.port.store.IEmailTemplatePort;
import serp.project.mailservice.core.service.IEmailTemplateService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateUseCases {

    private final IEmailTemplatePort emailTemplatePort;
    private final IEmailTemplateService emailTemplateService;
    private final IRedisCachePort redisCachePort;

    @Transactional
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request, Long tenantId, Long userId) {
        log.info("Creating email template: {} for tenant: {}", request.getCode(), tenantId);

        if (emailTemplatePort.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Template code already exists: " + request.getCode());
        }

        if (!emailTemplateService.validateTemplate(request.getBodyTemplate())) {
            throw new IllegalArgumentException("Invalid template syntax");
        }

        EmailTemplateEntity template = EmailTemplateMapper.toEntity(request, tenantId, userId);
        EmailTemplateEntity savedTemplate = emailTemplatePort.save(template);

        emailTemplateService.cacheTemplate(savedTemplate);

        log.info("Template created successfully: {}", savedTemplate.getId());
        return EmailTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplate(Long templateId) {
        log.debug("Getting email template: {}", templateId);

        EmailTemplateEntity template = emailTemplatePort.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        return EmailTemplateMapper.toResponse(template);
    }

    @Transactional
    public EmailTemplateResponse updateTemplate(Long templateId, EmailTemplateRequest request, Long tenantId, Long userId) {
        log.info("Updating email template: {}", templateId);

        EmailTemplateEntity template = emailTemplatePort.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + templateId));

        if (!template.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Template does not belong to tenant");
        }

        if (!template.getCode().equals(request.getCode()) && emailTemplatePort.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Template code already exists: " + request.getCode());
        }

        if (!emailTemplateService.validateTemplate(request.getBodyTemplate())) {
            throw new IllegalArgumentException("Invalid template syntax");
        }

        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setSubject(request.getSubject());
        template.setBodyTemplate(request.getBodyTemplate());
        template.setVersion(template.getVersion() + 1);

        EmailTemplateEntity updatedTemplate = emailTemplatePort.save(template);

        emailTemplateService.cacheTemplate(updatedTemplate);

        log.info("Email template updated: {}, version: {}", templateId, updatedTemplate.getVersion());
        return EmailTemplateMapper.toResponse(updatedTemplate);
    }

    @Transactional
    public void deleteTemplate(Long templateId, Long tenantId) {
        log.info("Deleting email template: {}", templateId);

        EmailTemplateEntity template = emailTemplatePort.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + templateId));

        if (!template.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Template does not belong to tenant");
        }

        template.setActiveStatus(ActiveStatus.DELETED);
        emailTemplatePort.save(template);

        String cacheKey = "email_template:" + templateId;
        redisCachePort.deleteFromCache(cacheKey);

        log.info("Email template deleted (soft delete): {}", templateId);
    }
}
