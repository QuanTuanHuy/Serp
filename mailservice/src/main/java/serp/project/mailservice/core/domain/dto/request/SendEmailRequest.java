/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.EmailPriority;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    @NotEmpty(message = "At least one recipient is required")
    private List<@Email(message = "Invalid email address") String> toEmails;

    private List<@Email String> ccEmails;
    private List<@Email String> bccEmails;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String body;
    private Boolean isHtml;

    private Long templateId;
    private Map<String, Object> templateVariables;

    private EmailType type;
    private EmailPriority priority;
    private EmailProvider provider;
    private Map<String, Object> metadata;

    private List<AttachmentRequest> attachments;
}
