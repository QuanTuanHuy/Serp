/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private String description;

    private String subject;
    private String bodyTemplate;
    private Boolean isHtml;

    private Map<String, Object> variablesSchema;
    private Map<String, Object> defaultValues;

    private EmailType type;
    private String language;
    private String category;
    private Boolean isGlobal;

    private Integer version;
    private Boolean isActive;

    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
