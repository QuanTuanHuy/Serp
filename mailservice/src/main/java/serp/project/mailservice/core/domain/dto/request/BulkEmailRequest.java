/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.EmailPriority;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmailRequest {
    @NotEmpty(message = "Recipients list cannot be empty")
    private List<RecipientRequest> recipients;

    private String subject;
    private String body;
    private Boolean isHtml;

    private Long templateId;

    private EmailType type;
    private EmailPriority priority;
    private Map<String, Object> metadata;
}
