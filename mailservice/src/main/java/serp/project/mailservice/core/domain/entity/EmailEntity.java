/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEntity {
    private Long id;
    private String messageId;
    private Long tenantId;
    private Long userId;

    private EmailProvider provider;
    private EmailStatus status;
    private EmailPriority priority;
    private EmailType type;

    private String fromEmail;
    private String fromName;
    private String replyTo;
    private List<String> toEmails;
    private List<String> ccEmails;
    private List<String> bccEmails;

    private String subject;
    private String body;
    private Boolean isHtml;

    private Long templateId;
    private Map<String, Object> templateVariables;

    private Map<String, Object> metadata;
    private String providerMessageId;
    private Map<String, Object> providerResponse;

    private LocalDateTime sentAt;
    private LocalDateTime failedAt;
    private LocalDateTime nextRetryAt;
    private Integer retryCount;
    private Integer maxRetries;
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ActiveStatus activeStatus;
}
