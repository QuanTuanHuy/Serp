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
import serp.project.mailservice.core.domain.enums.ActiveStatus;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateEntity extends BaseEntity {
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

    // ==================== Factory Methods ====================

    public static EmailTemplateEntity createNew(Long tenantId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return EmailTemplateEntity.builder()
                .tenantId(tenantId)
                .createdBy(userId)
                .updatedBy(userId)
                .isHtml(true)
                .isGlobal(false)
                .isActive(true)
                .version(1)
                .createdAt(now)
                .updatedAt(now)
                .activeStatus(ActiveStatus.ACTIVE)
                .build();
    }

    // ==================== Validation ====================

    public void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Template code is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Template name is required");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Template subject is required");
        }
        if (bodyTemplate == null || bodyTemplate.isBlank()) {
            throw new IllegalArgumentException("Template body is required");
        }
    }

    // ==================== Business Methods ====================

    public void incrementVersion() {
        this.version = (this.version != null ? this.version : 0) + 1;
        this.setUpdatedAt(LocalDateTime.now());
    }

    public boolean belongsToTenant(Long tenantId) {
        return this.tenantId != null && this.tenantId.equals(tenantId);
    }

    public boolean isGlobalTemplate() {
        return this.tenantId == null && Boolean.TRUE.equals(isGlobal);
    }

    public boolean isActiveTemplate() {
        return Boolean.TRUE.equals(isActive) && !isDeleted();
    }
}
