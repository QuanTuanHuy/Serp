/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.account.core.domain.dto.message;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailRequest {
    @Email(message = "Invalid email address")
    private String fromEmail;

    @NotEmpty(message = "At least one recipient is required")
    private List<@Email(message = "Invalid email address") String> toEmails;

    private List<@Email String> ccEmails;
    private List<@Email String> bccEmails;

    private String subject;

    private String body;
    private Boolean isHtml;

    private Long templateId;
    private String templateCode;
    private Map<String, Object> templateVariables;

    private String type;
    private String priority;
    @Builder.Default
    private String provider = "JAVA_MAIL";
    private Map<String, Object> metadata;

    public static SendEmailRequest resetPasswordEmail(
            String toEmail,
            String userName,
            String resetLink,
            Long expirationMinutes) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName == null || userName.isBlank() ? "User" : userName);
        variables.put("resetLink", resetLink);
        variables.put("expirationMinutes", expirationMinutes != null && expirationMinutes > 0 ? expirationMinutes : 60L);

        return SendEmailRequest.builder()
                .toEmails(List.of(toEmail))
                .templateCode("PASSWORD_RESET")
                .templateVariables(variables)
                .type("PASSWORD_RESET")
                .priority("HIGH")
                .provider("JAVA_MAIL")
                .build();
    }
}
