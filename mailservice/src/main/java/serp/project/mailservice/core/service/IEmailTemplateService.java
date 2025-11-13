/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.service;

import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;

import java.util.Map;

public interface IEmailTemplateService {
    String renderTemplate(Long templateId, Map<String, Object> variables);
    
    String renderTemplateByCode(Long tenantId, String templateCode, Map<String, Object> variables);
    
    void cacheTemplate(EmailTemplateEntity template);
    
    void invalidateCache(Long templateId);
    
    boolean validateTemplate(String templateContent);
}
