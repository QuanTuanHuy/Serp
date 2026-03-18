package serp.project.pmcore.application.command.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.command.project.validator.CreateProjectValidator;
import serp.project.pmcore.domain.dto.request.CreateProjectRequest;
import serp.project.pmcore.domain.dto.response.ProjectResponse;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.ISchemeProvisioningService;
import serp.project.pmcore.domain.validator.WorkflowSchemeCompatibilityValidator;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateProjectCommand {
    private final CreateProjectValidator projectValidator;
    private final WorkflowSchemeCompatibilityValidator workflowSchemeCompatibilityValidator;

    private final IProjectService projectService;
    private final ISchemeProvisioningService schemeProvisioningService;

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
                .build();
        ProjectEntity saved = projectService.createProject(project, tenantId, userId);

        Map<String, Long> schemeOverrides = buildSchemeOverrides(request);
        schemeProvisioningService.provisionSchemes(
                project,
                tenantId,
                userId,
                request.getBlueprintId(),
                schemeOverrides,
                request.getAssociationMode());
        ProjectEntity finalProject = projectService.saveProject(saved, userId);

        return toResponse(finalProject);
    }

    private Map<String, Long> buildSchemeOverrides(CreateProjectRequest request) {
        Map<String, Long> overrides = new HashMap<>();
        if (request.getIssueTypeSchemeId() != null) {
            overrides.put("ISSUE_TYPE", request.getIssueTypeSchemeId());
        }
        if (request.getPrioritySchemeId() != null) {
            overrides.put("PRIORITY", request.getPrioritySchemeId());
        }
        if (request.getWorkflowSchemeId() != null) {
            overrides.put("WORKFLOW", request.getWorkflowSchemeId());
        }
        if (request.getFieldConfigSchemeId() != null) {
            overrides.put("FIELD_CONFIG", request.getFieldConfigSchemeId());
        }
        if (request.getIssueTypeScreenSchemeId() != null) {
            overrides.put("SCREEN", request.getIssueTypeScreenSchemeId());
        }
        if (request.getPermissionSchemeId() != null) {
            overrides.put("PERMISSION", request.getPermissionSchemeId());
        }
        if (request.getNotificationSchemeId() != null) {
            overrides.put("NOTIFICATION", request.getNotificationSchemeId());
        }
        if (request.getIssueSecuritySchemeId() != null) {
            overrides.put("ISSUE_SECURITY", request.getIssueSecuritySchemeId());
        }
        return overrides;
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
