/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.workitem.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkItemCreateAuthorizationService {

    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    public ProjectPermissionEvaluationContext buildActorContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .build();
    }

    public void checkCreatePermissions(ProjectEntity project, ProjectPermissionEvaluationContext actorContext) {
        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS);
        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.CREATE_ISSUES);
    }

    public void checkScheduleIssuesPermissionIfNeeded(ProjectEntity project,
                                                      ProjectPermissionEvaluationContext actorContext,
                                                      Long dueDate) {
        if (dueDate != null) {
            projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.SCHEDULE_ISSUES);
        }
    }

    public Long resolveAssigneeId(ProjectEntity project,
                                  Long requestedAssigneeId,
                                  ProjectPermissionEvaluationContext actorContext) {
        if (requestedAssigneeId == null) {
            return null;
        }

        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.ASSIGN_ISSUES);

        ProjectPermissionEvaluationContext assigneeContext = ProjectPermissionEvaluationContext.builder()
                .userId(requestedAssigneeId)
                .build();

        if (!projectPermissionEvaluationService.hasPermission(project, assigneeContext, ProjectPermissionKeys.ASSIGNABLE_USER)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "Assignee is not assignable in project: projectId=" + project.getId() + ", assigneeId=" + requestedAssigneeId
            );
        }

        return requestedAssigneeId;
    }

    public void checkSetIssueSecurityPermissionIfNeeded(ProjectEntity project,
                                                        ProjectPermissionEvaluationContext actorContext,
                                                        Long requestedSecurityLevelId) {
        if (requestedSecurityLevelId != null) {
            projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.SET_ISSUE_SECURITY);
        }
    }
}
