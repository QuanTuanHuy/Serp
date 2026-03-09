/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;

import java.util.Map;
import java.util.Optional;

public interface IEmailTemplateService {
    EmailTemplateEntity save(EmailTemplateEntity template);

    Optional<EmailTemplateEntity> getTemplateById(Long templateId);

    String renderTemplate(String bodyTemplate, Map<String, Object> defaultValues, Map<String, Object> variables);

    void cacheTemplate(EmailTemplateEntity template);

    void invalidateCache(Long templateId);

    boolean validateTemplate(String templateContent);

    boolean existsByCode(String code);
}
