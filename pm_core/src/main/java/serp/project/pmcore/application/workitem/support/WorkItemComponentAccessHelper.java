/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WorkItemComponentAccessHelper {

    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;
    private final IWorkItemService workItemService;

    public Context requireReadableWorkItem(Long projectId,
                                           Long workItemId,
                                           Long tenantId,
                                           Long userId,
                                           Set<String> groupKeys) {
        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        WorkItemEntity workItem = workItemService.getWorkItemById(workItemId, tenantId);
        ensureWorkItemBelongsToProject(workItem, projectId);

        ProjectPermissionEvaluationContext actorContext =
                workItemAuthorizationSupportService.buildActorContext(
                        userId,
                        groupKeys,
                        workItem.getReporterId(),
                        workItem.getAssigneeId()
                );
        ProjectPermissionSubject permissionSubject = ProjectPermissionSubject.from(project);
        projectPermissionEvaluationService.checkPermission(
                permissionSubject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);

        return new Context(project, workItem, permissionSubject, actorContext);
    }

    public Context requireEditableWorkItem(Long projectId,
                                           Long workItemId,
                                           Long tenantId,
                                           Long userId,
                                           Set<String> groupKeys) {
        Context context = requireReadableWorkItem(projectId, workItemId, tenantId, userId, groupKeys);
        if (Boolean.TRUE.equals(context.project().getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
        projectPermissionEvaluationService.checkPermission(
                context.permissionSubject(),
                context.actorContext(),
                ProjectPermissionKeys.EDIT_ISSUES
        );
        return context;
    }

    public Map<Long, ProjectComponentEntity> validateComponentsBelongToProject(List<ProjectComponentEntity> components,
                                                                               Long projectId) {
        Map<Long, ProjectComponentEntity> byId = components.stream()
                .collect(Collectors.toMap(
                        ProjectComponentEntity::getId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        for (ProjectComponentEntity component : components) {
            if (!Objects.equals(component.getProjectId(), projectId)) {
                throw new ResourceNotFoundException(
                        DomainErrorCode.COMPONENT_NOT_FOUND,
                        "Component does not belong to project: componentId=" + component.getId()
                                + ", projectId=" + projectId
                );
            }
        }
        return byId;
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, Long projectId) {
        if (!Objects.equals(workItem.getProjectId(), projectId)) {
            throw ResourceNotFoundException.workItem(workItem.getId());
        }
    }

    public record Context(
            ProjectEntity project,
            WorkItemEntity workItem,
            ProjectPermissionSubject permissionSubject,
            ProjectPermissionEvaluationContext actorContext
    ) {
    }
}
