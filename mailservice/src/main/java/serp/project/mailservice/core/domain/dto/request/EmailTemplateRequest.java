/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateRequest {
    @NotBlank(message = "Template name is required")
    private String name;
    
    @NotBlank(message = "Template code is required")
    private String code;
    
    private String description;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Body template is required")
    private String bodyTemplate;
    
    private Boolean isHtml;
    private Map<String, Object> variablesSchema;
    private Map<String, Object> defaultValues;
    private EmailType type;
    private String language;
    private String category;
    private Boolean isGlobal;
}
