/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.command.project.validator.CreateProjectValidator;
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.project.ProjectResponse;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.service.IProjectService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProjectCommand {
    private final CreateProjectValidator projectValidator;
    private final IProjectService projectService;

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse execute(CreateProjectRequest request, Long userId, Long tenantId) {
        projectValidator.validate(request, tenantId);

        ProjectEntity project = ProjectEntity.builder()
                .key(request.getKey())
                .name(request.getName())
                .description(request.getDescription())
                .url(request.getUrl())
                .leadUserId(request.getLeadUserId())
                .avatarId(request.getAvatarId())
                .categoryId(request.getCategoryId())
                .projectTypeKey(request.getProjectTypeKey())
                .issueTypeSchemeId(request.getIssueTypeSchemeId())
                .workflowSchemeId(request.getWorkflowSchemeId())
                .fieldConfigSchemeId(request.getFieldConfigSchemeId())
                .issueTypeScreenSchemeId(request.getIssueTypeScreenSchemeId())
                .permissionSchemeId(request.getPermissionSchemeId())
                .notificationSchemeId(request.getNotificationSchemeId())
                .prioritySchemeId(request.getPrioritySchemeId())
                .issueSecuritySchemeId(request.getIssueSecuritySchemeId())
                .build();

        ProjectEntity saved = projectService.createProject(project, tenantId, userId);
        return toResponse(saved);
    }

    private ProjectResponse toResponse(ProjectEntity entity) {
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
