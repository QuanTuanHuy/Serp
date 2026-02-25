/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.core.domain.dto.request.CreateProjectRequest;
import serp.project.pmcore.core.domain.dto.request.GetProjectParams;
import serp.project.pmcore.core.domain.dto.request.UpdateProjectRequest;
import serp.project.pmcore.core.domain.dto.response.ProjectResponse;
import serp.project.pmcore.core.domain.entity.ProjectEntity;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.client.IKafkaPublisher;
import serp.project.pmcore.core.service.IProjectBlueprintService;
import serp.project.pmcore.core.service.IProjectService;
import serp.project.pmcore.core.service.ISchemeProvisioningService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectUseCase {

    private static final String PROJECT_EVENTS_TOPIC = "serp.pm.project.events";

    private final IProjectService projectService;
    private final IProjectBlueprintService projectBlueprintService;
    private final ISchemeProvisioningService schemeProvisioningService;
    private final IKafkaPublisher kafkaPublisher;

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse createProject(CreateProjectRequest request, Long tenantId, Long userId) {
        projectService.validateKeyFormat(request.getKey());
        projectService.validateKeyUniqueness(request.getKey(), tenantId);

        projectService.validateCategoryExists(request.getCategoryId(), tenantId);

        if (request.getBlueprintId() != null) {
            projectBlueprintService.getBlueprintById(request.getBlueprintId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.BLUEPRINT_NOT_FOUND));
        }

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
        schemeProvisioningService.provisionSchemes(saved, tenantId, userId,
                request.getBlueprintId(), schemeOverrides);

        ProjectEntity finalProject = projectService.saveProject(saved, userId);

        publishProjectEvent("PROJECT_CREATED", finalProject);

        return toResponse(finalProject);
    }

    public ProjectResponse getProjectById(Long id, Long tenantId) {
        ProjectEntity project = projectService.getProjectById(id, tenantId);
        return toResponse(project);
    }

    public ProjectResponse getProjectByKey(String key, Long tenantId) {
        ProjectEntity project = projectService.getProjectByKey(key, tenantId);
        return toResponse(project);
    }

    public Map<String, Object> getProjects(Long tenantId, GetProjectParams params) {
        Pair<List<ProjectEntity>, Long> result = projectService.getProjects(tenantId, params);
        List<ProjectResponse> responses = result.getFirst().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return Map.of(
                "totalItems", result.getSecond(),
                "items", responses
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request,
                                          Long tenantId, Long userId) {
        ProjectEntity updateData = ProjectEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .leadUserId(request.getLeadUserId())
                .categoryId(request.getCategoryId())
                .url(request.getUrl())
                .avatarId(request.getAvatarId())
                .build();

        ProjectEntity updated = projectService.updateProject(id, updateData, tenantId, userId);

        publishProjectEvent("PROJECT_UPDATED", updated);

        return toResponse(updated);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id, Long tenantId) {
        ProjectEntity project = projectService.getProjectById(id, tenantId);
        projectService.deleteProject(id, tenantId);

        publishProjectEvent("PROJECT_DELETED", project);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse archiveProject(Long id, Long tenantId, Long userId) {
        ProjectEntity archived = projectService.archiveProject(id, tenantId, userId);

        publishProjectEvent("PROJECT_ARCHIVED", archived);

        return toResponse(archived);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectResponse unarchiveProject(Long id, Long tenantId, Long userId) {
        ProjectEntity unarchived = projectService.unarchiveProject(id, tenantId, userId);

        publishProjectEvent("PROJECT_UNARCHIVED", unarchived);

        return toResponse(unarchived);
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

    private void publishProjectEvent(String eventType, ProjectEntity project) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", eventType,
                    "projectId", project.getId(),
                    "projectKey", project.getKey(),
                    "tenantId", project.getTenantId(),
                    "timestamp", System.currentTimeMillis()
            );
            kafkaPublisher.sendMessageAsync(
                    project.getId().toString(), event, PROJECT_EVENTS_TOPIC);
            log.info("Published {} event for project id={}, key={}",
                    eventType, project.getId(), project.getKey());
        } catch (Exception e) {
            log.error("Failed to publish {} event for project id={}: {}",
                    eventType, project.getId(), e.getMessage(), e);
            // Don't fail the transaction for Kafka publish failure
        }
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
