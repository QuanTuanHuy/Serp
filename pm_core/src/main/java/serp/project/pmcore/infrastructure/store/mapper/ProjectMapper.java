/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectEntity;
import serp.project.pmcore.infrastructure.store.model.ProjectModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper extends BaseMapper {

    public ProjectEntity toEntity(ProjectModel model) {
        if (model == null) { return null; }
        return ProjectEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .key(model.getKey())
                .name(model.getName())
                .description(model.getDescription())
                .url(model.getUrl())
                .leadUserId(model.getLeadUserId())
                .avatarId(model.getAvatarId())
                .categoryId(model.getProjectCategoryId())
                .projectTypeKey(model.getProjectTypeKey())
                .isArchived(model.getArchived())
                .archivedAt(localDateTimeToLong(model.getArchivedAt()))
                .issueTypeSchemeId(model.getIssueTypeSchemeId())
                .workflowSchemeId(model.getWorkflowSchemeId())
                .fieldConfigSchemeId(model.getFieldConfigSchemeId())
                .issueTypeScreenSchemeId(model.getIssueTypeScreenSchemeId())
                .permissionSchemeId(model.getPermissionSchemeId())
                .notificationSchemeId(model.getNotificationSchemeId())
                .prioritySchemeId(model.getPrioritySchemeId())
                .issueSecuritySchemeId(model.getIssueSecuritySchemeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ProjectModel toModel(ProjectEntity entity) {
        if (entity == null) { return null; }
        return ProjectModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .key(entity.getKey())
                .name(entity.getName())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .leadUserId(entity.getLeadUserId())
                .avatarId(entity.getAvatarId())
                .projectCategoryId(entity.getCategoryId())
                .projectTypeKey(entity.getProjectTypeKey())
                .archived(entity.getIsArchived())
                .archivedAt(longToLocalDateTime(entity.getArchivedAt()))
                .issueTypeSchemeId(entity.getIssueTypeSchemeId())
                .workflowSchemeId(entity.getWorkflowSchemeId())
                .fieldConfigSchemeId(entity.getFieldConfigSchemeId())
                .issueTypeScreenSchemeId(entity.getIssueTypeScreenSchemeId())
                .permissionSchemeId(entity.getPermissionSchemeId())
                .notificationSchemeId(entity.getNotificationSchemeId())
                .prioritySchemeId(entity.getPrioritySchemeId())
                .issueSecuritySchemeId(entity.getIssueSecuritySchemeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ProjectEntity> toEntities(List<ProjectModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
