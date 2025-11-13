/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.EmailPriority;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatusResponse {
    private Long id;
    private String messageId;
    private EmailStatus status;
    private EmailProvider provider;
    private EmailType type;
    private EmailPriority priority;

    private List<String> toEmails;
    private String subject;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime failedAt;
    private LocalDateTime nextRetryAt;

    private Integer retryCount;
    private Integer maxRetries;
    private String errorMessage;
    private Map<String, Object> providerResponse;
}
