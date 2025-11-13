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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService implements IEmailTemplateService {

    private final IEmailTemplatePort emailTemplatePort;
    private final ITemplateEnginePort templateEnginePort;
    private final IRedisCachePort redisCachePort;

    private static final Duration TEMPLATE_CACHE_TTL = Duration.ofHours(1);

    @Override
    public String renderTemplate(Long templateId, Map<String, Object> variables) {
        log.debug("Rendering template ID: {} with {} variables", templateId, 
                variables != null ? variables.size() : 0);

        EmailTemplateEntity template = getTemplate(templateId);
        
        if (template == null) {
            throw new IllegalArgumentException("Template not found with ID: " + templateId);
        }

        Map<String, Object> mergedVariables = mergeVariables(template, variables);

        String renderedHtml = templateEnginePort.processTemplate(
                template.getBodyTemplate(), 
                mergedVariables
        );

        log.debug("Template rendered successfully: {}", templateId);
        return renderedHtml;
    }

    @Override
    public String renderTemplateByCode(Long tenantId, String templateCode, Map<String, Object> variables) {
        log.debug("Rendering template code: {} for tenant: {}", templateCode, tenantId);

        EmailTemplateEntity template = emailTemplatePort.findByTenantIdAndCode(tenantId, templateCode)
                .or(() -> emailTemplatePort.findByCode(templateCode))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Template not found with code: " + templateCode + " for tenant: " + tenantId)
                );

        cacheTemplate(template);

        Map<String, Object> mergedVariables = mergeVariables(template, variables);
        String renderedHtml = templateEnginePort.processTemplate(
                template.getBodyTemplate(),
                mergedVariables
        );

        log.debug("Template code rendered successfully: {}", templateCode);
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

    private EmailTemplateEntity getTemplate(Long templateId) {
        String cacheKey = RedisKey.TEMPLATE_CACHE_PREFIX + templateId;
        
        try {
            EmailTemplateEntity cachedTemplate = redisCachePort.getFromCache(
                    cacheKey, 
                    EmailTemplateEntity.class
            );
            
            if (cachedTemplate != null) {
                log.debug("Template ID: {} retrieved from cache", templateId);
                return cachedTemplate;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve template from cache: {}", templateId, e);
        }

        EmailTemplateEntity template = emailTemplatePort.findById(templateId).orElse(null);
        
        if (template != null) {
            cacheTemplate(template);
            log.debug("Template ID: {} retrieved from database", templateId);
        }
        
        return template;
    }

    private Map<String, Object> mergeVariables(EmailTemplateEntity template, Map<String, Object> variables) {
        Map<String, Object> merged = template.getDefaultValues() != null 
                ? Map.copyOf(template.getDefaultValues()) 
                : Map.of();
        
        if (variables != null && !variables.isEmpty()) {
            merged = new java.util.HashMap<>(merged);
            merged.putAll(variables);
        }

        log.debug("Merged {} default values with {} provided variables", 
                template.getDefaultValues() != null ? template.getDefaultValues().size() : 0,
                variables != null ? variables.size() : 0);
        
        return merged;
    }
}
