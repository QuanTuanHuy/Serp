/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkItemAuthorizationSupportService implements IWorkItemAuthorizationSupportService {

    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    public ProjectPermissionEvaluationContext buildActorContext(Long userId, Set<String> groupKeys) {
        return buildActorContext(userId, groupKeys, null, null);
    }

    @Override
    public ProjectPermissionEvaluationContext buildActorContext(Long userId,
                                                                Set<String> groupKeys,
                                                                Long reporterUserId,
                                                                Long assigneeUserId) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys == null ? Set.of() : groupKeys)
                .reporterUserId(reporterUserId)
                .assigneeUserId(assigneeUserId)
                .build();
    }

    @Override
    public void checkRequiredPermissions(ProjectPermissionSubject subject,
                                         ProjectPermissionEvaluationContext actorContext,
                                         String... permissionKeys) {
        for (String permissionKey : permissionKeys) {
            projectPermissionEvaluationService.checkPermission(subject, actorContext, permissionKey);
        }
    }

    @Override
    public void checkScheduleIssuesPermission(ProjectPermissionSubject subject,
                                              ProjectPermissionEvaluationContext actorContext) {
        projectPermissionEvaluationService.checkPermission(subject, actorContext, ProjectPermissionKeys.SCHEDULE_ISSUES);
    }

    @Override
    public Long resolveAssigneeId(ProjectPermissionSubject subject,
                                  Long requestedAssigneeId,
                                  ProjectPermissionEvaluationContext actorContext) {
        if (requestedAssigneeId == null) {
            return null;
        }

        projectPermissionEvaluationService.checkPermission(subject, actorContext, ProjectPermissionKeys.ASSIGN_ISSUES);

        ProjectPermissionEvaluationContext assigneeContext = ProjectPermissionEvaluationContext.builder()
                .userId(requestedAssigneeId)
                .build();

        if (!projectPermissionEvaluationService.hasPermission(subject, assigneeContext, ProjectPermissionKeys.ASSIGNABLE_USER)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "Assignee is not assignable in project: projectId=" + subject.projectId() + ", assigneeId=" + requestedAssigneeId
            );
        }

        return requestedAssigneeId;
    }

    @Override
    public void checkSetIssueSecurityPermissionIfNeeded(ProjectPermissionSubject subject,
                                                        ProjectPermissionEvaluationContext actorContext,
                                                        Long requestedSecurityLevelId) {
        if (requestedSecurityLevelId != null) {
            projectPermissionEvaluationService.checkPermission(subject, actorContext, ProjectPermissionKeys.SET_ISSUE_SECURITY);
        }
    }
}
