/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.client;

import java.util.Map;

public interface ITemplateEnginePort {
    /**
     * Process template with variables
     * @param templateContent Template content (Thymeleaf format)
     * @param variables Map of variables to replace
     * @return Processed template string
     */
    String processTemplate(String templateContent, Map<String, Object> variables);
    
    /**
     * Validate template syntax
     * @param templateContent Template content
     * @return true if template is valid
     */
    boolean validateTemplate(String templateContent);
}
