/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ProjectModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "key", nullable = false, length = 20)
    private String key;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "url")
    private String url;

    @Column(name = "lead_user_id")
    private Long leadUserId;

    @Column(name = "avatar_id")
    private Long avatarId;

    @Column(name = "project_category_id")
    private Long projectCategoryId;

    @Column(name = "project_type_key", length = 50)
    private String projectTypeKey;

    @Column(name = "archived")
    private Boolean archived;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "issue_type_scheme_id")
    private Long issueTypeSchemeId;

    @Column(name = "workflow_scheme_id")
    private Long workflowSchemeId;

    @Column(name = "field_config_scheme_id")
    private Long fieldConfigSchemeId;

    @Column(name = "issue_type_screen_scheme_id")
    private Long issueTypeScreenSchemeId;

    @Column(name = "permission_scheme_id")
    private Long permissionSchemeId;

    @Column(name = "notification_scheme_id")
    private Long notificationSchemeId;

    @Column(name = "priority_scheme_id")
    private Long prioritySchemeId;

    @Column(name = "issue_security_scheme_id")
    private Long issueSecuritySchemeId;
}
