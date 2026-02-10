/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import serp.project.mailservice.core.domain.enums.ActiveStatus;
import serp.project.mailservice.core.domain.enums.EmailPriority;
import serp.project.mailservice.core.domain.enums.EmailProvider;
import serp.project.mailservice.core.domain.enums.EmailStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "emails", indexes = {
    @Index(name = "idx_message_id", columnList = "message_id"),
    @Index(name = "idx_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status_retry", columnList = "status, next_retry_at, retry_count"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true, nullable = false, length = 100)
    private String messageId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20)
    private EmailProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    private EmailPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private EmailType type;

    @Column(name = "from_email", nullable = false, length = 255)
    private String fromEmail;

    @Column(name = "from_name", length = 255)
    private String fromName;

    @Column(name = "reply_to", length = 255)
    private String replyTo;

    @Type(JsonType.class)
    @Column(name = "to_emails", columnDefinition = "TEXT[]")
    private List<String> toEmails;

    @Type(JsonType.class)
    @Column(name = "cc_emails", columnDefinition = "TEXT[]")
    private List<String> ccEmails;

    @Type(JsonType.class)
    @Column(name = "bcc_emails", columnDefinition = "TEXT[]")
    private List<String> bccEmails;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_html")
    private Boolean isHtml;

    @Column(name = "template_id")
    private Long templateId;

    @Type(JsonType.class)
    @Column(name = "template_variables", columnDefinition = "jsonb")
    private Map<String, Object> templateVariables;

    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Type(JsonType.class)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    private Map<String, Object> providerResponse;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "max_retries")
    private Integer maxRetries;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_status", nullable = false, length = 10)
    private ActiveStatus activeStatus;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (activeStatus == null) {
            activeStatus = ActiveStatus.ACTIVE;
        }
        if (status == null) {
            status = EmailStatus.PENDING;
        }
        if (isHtml == null) {
            isHtml = true;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (maxRetries == null) {
            maxRetries = 3;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
