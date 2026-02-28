/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProjectEntity extends BaseEntity {
    private Long tenantId;
    private String key;
    private String name;
    private String description;
    private String url;
    private Long leadUserId;
    private Long avatarId;
    private Long categoryId;
    private String projectTypeKey;
    private Boolean isArchived;
    private Long archivedAt;

    // Scheme bindings (project-owned clones)
    private Long issueTypeSchemeId;
    private Long workflowSchemeId;
    private Long fieldConfigSchemeId;
    private Long issueTypeScreenSchemeId;
    private Long permissionSchemeId;
    private Long notificationSchemeId;
    private Long prioritySchemeId;
    private Long issueSecuritySchemeId;
}
