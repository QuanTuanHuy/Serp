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
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "email_templates", indexes = {
    @Index(name = "idx_template_code", columnList = "code"),
    @Index(name = "idx_tenant_code", columnList = "tenant_id, code"),
    @Index(name = "idx_template_type", columnList = "type"),
    @Index(name = "idx_is_global", columnList = "is_global")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "body_template", nullable = false, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "is_html")
    private Boolean isHtml;

    @Type(JsonType.class)
    @Column(name = "variables_schema", columnDefinition = "jsonb")
    private Map<String, Object> variablesSchema;

    @Type(JsonType.class)
    @Column(name = "default_values", columnDefinition = "jsonb")
    private Map<String, Object> defaultValues;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30)
    private EmailType type;

    @Column(name = "language", length = 10)
    private String language;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_global")
    private Boolean isGlobal;

    @Column(name = "version")
    private Integer version;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

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
        if (isHtml == null) {
            isHtml = true;
        }
        if (isGlobal == null) {
            isGlobal = false;
        }
        if (version == null) {
            version = 1;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
