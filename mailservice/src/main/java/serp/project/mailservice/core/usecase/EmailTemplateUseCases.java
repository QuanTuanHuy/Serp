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
import serp.project.mailservice.core.domain.mapper.EmailTemplateMapper;
import serp.project.mailservice.core.exception.AppException;
import serp.project.mailservice.core.exception.ErrorCode;
import serp.project.mailservice.core.service.IEmailTemplateService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateUseCases {

    private final IEmailTemplateService emailTemplateService;

    @Transactional
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request, Long tenantId, Long userId) {
        log.info("Creating email template: {} for tenant: {}", request.getCode(), tenantId);

        if (emailTemplateService.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.TEMPLATE_CODE_ALREADY_EXISTS,
                    "Template code already exists: " + request.getCode());
        }

        if (!emailTemplateService.validateTemplate(request.getBodyTemplate())) {
            throw new AppException(ErrorCode.INVALID_TEMPLATE_SYNTAX);
        }

        EmailTemplateEntity template = EmailTemplateMapper.toEntity(request, tenantId, userId);
        EmailTemplateEntity savedTemplate = emailTemplateService.save(template);

        emailTemplateService.cacheTemplate(savedTemplate);

        log.info("Template created successfully: {}", savedTemplate.getId());
        return EmailTemplateMapper.toResponse(savedTemplate);
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplate(Long templateId) {
        log.debug("Getting email template: {}", templateId);

        EmailTemplateEntity template = emailTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND,
                        "Template not found: " + templateId));

        return EmailTemplateMapper.toResponse(template);
    }

    @Transactional
    public EmailTemplateResponse updateTemplate(Long templateId, EmailTemplateRequest request, Long tenantId, Long userId) {
        log.info("Updating email template: {}", templateId);

        EmailTemplateEntity template = emailTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND,
                        "Template not found with id: " + templateId));

        if (!template.belongsToTenant(tenantId)) {
            throw new AppException(ErrorCode.TEMPLATE_NOT_BELONG_TO_TENANT);
        }

        if (!template.getCode().equals(request.getCode()) && emailTemplateService.existsByCode(request.getCode())) {
            throw new AppException(ErrorCode.TEMPLATE_CODE_ALREADY_EXISTS,
                    "Template code already exists: " + request.getCode());
        }

        if (!emailTemplateService.validateTemplate(request.getBodyTemplate())) {
            throw new AppException(ErrorCode.INVALID_TEMPLATE_SYNTAX);
        }

        template.setCode(request.getCode());
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setSubject(request.getSubject());
        template.setBodyTemplate(request.getBodyTemplate());
        template.incrementVersion();

        EmailTemplateEntity updatedTemplate = emailTemplateService.save(template);

        emailTemplateService.cacheTemplate(updatedTemplate);

        log.info("Email template updated: {}, version: {}", templateId, updatedTemplate.getVersion());
        return EmailTemplateMapper.toResponse(updatedTemplate);
    }

    @Transactional
    public void deleteTemplate(Long templateId, Long tenantId) {
        log.info("Deleting email template: {}", templateId);

        EmailTemplateEntity template = emailTemplateService.getTemplateById(templateId)
                .orElseThrow(() -> new AppException(ErrorCode.TEMPLATE_NOT_FOUND,
                        "Template not found with id: " + templateId));

        if (!template.belongsToTenant(tenantId)) {
            throw new AppException(ErrorCode.TEMPLATE_NOT_BELONG_TO_TENANT);
        }

        template.markAsDeleted();
        emailTemplateService.save(template);

        emailTemplateService.invalidateCache(templateId);

        log.info("Email template deleted (soft delete): {}", templateId);
    }
}
