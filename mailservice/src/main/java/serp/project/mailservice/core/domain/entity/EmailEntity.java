/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import serp.project.mailservice.core.domain.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEntity extends BaseEntity {
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

    // ==================== Constants ====================

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final int DEFAULT_MAX_RETRIES = 3;

    // ==================== Factory Methods ====================

    public static EmailEntity createNew(Long tenantId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return EmailEntity.builder()
                .tenantId(tenantId)
                .userId(userId)
                .messageId(generateMessageId())
                .status(EmailStatus.PENDING)
                .priority(EmailPriority.NORMAL)
                .isHtml(true)
                .retryCount(0)
                .maxRetries(DEFAULT_MAX_RETRIES)
                .createdAt(now)
                .updatedAt(now)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    // ==================== Validation ====================

    public void validate() {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalArgumentException("From email is required");
        }
        if (!isValidEmailAddress(fromEmail)) {
            throw new IllegalArgumentException("Invalid from email format: " + fromEmail);
        }

        if (toEmails == null || toEmails.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient email is required");
        }
        for (String toEmail : toEmails) {
            if (!isValidEmailAddress(toEmail)) {
                throw new IllegalArgumentException("Invalid recipient email format: " + toEmail);
            }
        }

        if (ccEmails != null) {
            for (String ccEmail : ccEmails) {
                if (!isValidEmailAddress(ccEmail)) {
                    throw new IllegalArgumentException("Invalid CC email format: " + ccEmail);
                }
            }
        }

        if (bccEmails != null) {
            for (String bccEmail : bccEmails) {
                if (!isValidEmailAddress(bccEmail)) {
                    throw new IllegalArgumentException("Invalid BCC email format: " + bccEmail);
                }
            }
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Email subject is required");
        }

        if ((body == null || body.isBlank()) && templateId == null) {
            throw new IllegalArgumentException("Either email body or template ID must be provided");
        }
    }

    // ==================== State Transitions ====================

    public void markAsSent(String providerMessageId, Map<String, Object> providerResponse) {
        if (status != null && !status.canTransitionTo(EmailStatus.SENT)) {
            throw new IllegalStateException("Cannot transition from " + status + " to SENT");
        }
        this.status = EmailStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.providerMessageId = providerMessageId;
        this.providerResponse = providerResponse;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void markAsFailed(String errorMessage) {
        this.status = EmailStatus.FAILED;
        this.errorMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void scheduleRetry(String errorMessage) {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
        this.errorMessage = errorMessage;

        if (this.retryCount >= this.maxRetries) {
            this.status = EmailStatus.FAILED;
            this.failedAt = LocalDateTime.now();
        } else {
            this.status = EmailStatus.RETRY;
            long delayMinutes = (long) Math.pow(2, this.retryCount);
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        }
        this.setUpdatedAt(LocalDateTime.now());
    }

    public void cancel() {
        if (status != null && !status.canTransitionTo(EmailStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel email with status: " + status);
        }
        this.status = EmailStatus.CANCELLED;
        this.setUpdatedAt(LocalDateTime.now());
    }

    // ==================== Query Methods ====================

    public boolean isRetryable() {
        return status != null && status.isRetryable()
                && retryCount != null && maxRetries != null
                && retryCount < maxRetries;
    }

    public boolean isHighPriority() {
        return priority != null && priority.isHighPriority();
    }

    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    public boolean canTransitionTo(EmailStatus target) {
        return status != null && status.canTransitionTo(target);
    }

    // ==================== Enrichment ====================

    public void enrichDefaults() {
        if (messageId == null || messageId.isBlank()) {
            messageId = generateMessageId();
        }
        if (priority == null) {
            priority = EmailPriority.NORMAL;
        }
        if (isHtml == null) {
            isHtml = true;
        }
        if (maxRetries == null) {
            maxRetries = DEFAULT_MAX_RETRIES;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (status == null) {
            status = EmailStatus.PENDING;
        }
        initializeDefaults();
    }

    // ==================== Private Helpers ====================

    private static boolean isValidEmailAddress(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    private static String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
