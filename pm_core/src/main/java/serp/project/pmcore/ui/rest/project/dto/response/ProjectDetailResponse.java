/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.entity.project.ProjectEntity;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailResponse {

    private Long   id;
    private String key;
    private String name;
    private String description;
    private String url;
    private Long   leadUserId;
    private Long   avatarId;
    private String projectTypeKey;
    private Boolean isArchived;
    private Long   archivedAt;

    private CategorySummary category;

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

    public static ProjectDetailResponse from(ProjectEntity entity,
                                             CategorySummary category) {
        return ProjectDetailResponse.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .name(entity.getName())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .leadUserId(entity.getLeadUserId())
                .avatarId(entity.getAvatarId())
                .projectTypeKey(entity.getProjectTypeKey())
                .isArchived(entity.getIsArchived())
                .archivedAt(entity.getArchivedAt())
                .category(category)
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
