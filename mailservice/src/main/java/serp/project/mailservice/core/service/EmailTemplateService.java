/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.mailservice.core.domain.constant.RedisKey;
import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.core.port.client.IRedisCachePort;
import serp.project.mailservice.core.port.client.ITemplateEnginePort;
import serp.project.mailservice.core.port.store.IEmailTemplatePort;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService implements IEmailTemplateService {

    private final ITemplateEnginePort templateEnginePort;
    private final IRedisCachePort redisCachePort;
    private final IEmailTemplatePort emailTemplatePort;

    private static final Duration TEMPLATE_CACHE_TTL = Duration.ofHours(1);

    @Override
    public EmailTemplateEntity save(EmailTemplateEntity template) {
        return emailTemplatePort.save(template);
    }

    @Override
    public Optional<EmailTemplateEntity> getTemplateById(Long templateId) {
        if (templateId == null) {
            return Optional.empty();
        }

        String cacheKey = RedisKey.TEMPLATE_CACHE_PREFIX + templateId;
        try {
            EmailTemplateEntity cachedTemplate = redisCachePort.getFromCache(cacheKey, EmailTemplateEntity.class);
            if (cachedTemplate != null) {
                log.debug("Template ID: {} found in cache", templateId);
                return Optional.of(cachedTemplate);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve template ID: {} from cache", templateId, e);
        }

        Optional<EmailTemplateEntity> dbTemplateOpt = emailTemplatePort.findById(templateId);
        if (dbTemplateOpt.isPresent()) {
            EmailTemplateEntity dbTemplate = dbTemplateOpt.get();
            log.debug("Template ID: {} retrieved from database", templateId);
            cacheTemplate(dbTemplate);
            return Optional.of(dbTemplate);
        }

        log.warn("Template ID: {} not found", templateId);
        return Optional.empty();
    }

    @Override
    public String renderTemplate(String bodyTemplate, Map<String, Object> defaultValues, Map<String, Object> variables) {
        Map<String, Object> mergedVariables = mergeVariables(defaultValues, variables);

        String renderedHtml = templateEnginePort.processTemplate(bodyTemplate, mergedVariables);

        log.debug("Template rendered successfully");
        return renderedHtml;
    }

    @Override
    public void cacheTemplate(EmailTemplateEntity template) {
        if (template == null || template.getId() == null) {
            return;
        }

        String cacheKey = RedisKey.TEMPLATE_CACHE_PREFIX + template.getId();

        try {
            redisCachePort.setToCache(cacheKey, template, TEMPLATE_CACHE_TTL.toSeconds());
            log.debug("Cached template ID: {} for {} seconds", template.getId(), TEMPLATE_CACHE_TTL.toSeconds());
        } catch (Exception e) {
            log.error("Failed to cache template ID: {}", template.getId(), e);
        }
    }

    @Override
    public void invalidateCache(Long templateId) {
        if (templateId == null) {
            return;
        }

        String cacheKey = RedisKey.TEMPLATE_CACHE_PREFIX + templateId;

        try {
            redisCachePort.deleteFromCache(cacheKey);
            log.debug("Invalidated cache for template ID: {}", templateId);
        } catch (Exception e) {
            log.error("Failed to invalidate cache for template ID: {}", templateId, e);
        }
    }

    @Override
    public boolean validateTemplate(String templateContent) {
        try {
            return templateEnginePort.validateTemplate(templateContent);
        } catch (Exception e) {
            log.error("Template validation failed", e);
            return false;
        }
    }

    @Override
    public boolean existsByCode(String code) {
        return emailTemplatePort.existsByCode(code);
    }

    private Map<String, Object> mergeVariables(Map<String, Object> defaultValues, Map<String, Object> variables) {
        Map<String, Object> merged = new HashMap<>();

        if (defaultValues != null) {
            merged.putAll(defaultValues);
        }

        if (variables != null) {
            merged.putAll(variables);
        }

        return merged;
    }
}
