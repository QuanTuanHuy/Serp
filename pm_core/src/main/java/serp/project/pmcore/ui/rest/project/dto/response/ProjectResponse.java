/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.entity.project.ProjectEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
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

    // Scheme bindings
    private Long issueTypeSchemeId;
    private Long workflowSchemeId;
    private Long fieldConfigSchemeId;
    private Long issueTypeScreenSchemeId;
    private Long permissionSchemeId;
    private Long notificationSchemeId;
    private Long prioritySchemeId;
    private Long issueSecuritySchemeId;

    private Long createdAt;
    private Long createdBy;
    private Long updatedAt;
    private Long updatedBy;

    public static ProjectResponse from(ProjectEntity entity) {
        return ProjectResponse.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .name(entity.getName())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .leadUserId(entity.getLeadUserId())
                .avatarId(entity.getAvatarId())
                .categoryId(entity.getCategoryId())
                .projectTypeKey(entity.getProjectTypeKey())
                .isArchived(entity.getIsArchived())
                .archivedAt(entity.getArchivedAt())
                .issueTypeSchemeId(entity.getIssueTypeSchemeId())
                .workflowSchemeId(entity.getWorkflowSchemeId())
                .fieldConfigSchemeId(entity.getFieldConfigSchemeId())
                .issueTypeScreenSchemeId(entity.getIssueTypeScreenSchemeId())
                .permissionSchemeId(entity.getPermissionSchemeId())
                .notificationSchemeId(entity.getNotificationSchemeId())
                .prioritySchemeId(entity.getPrioritySchemeId())
                .issueSecuritySchemeId(entity.getIssueSecuritySchemeId())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
