/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;

import java.util.Set;

public interface IWorkItemAuthorizationSupportService {
    ProjectPermissionEvaluationContext buildActorContext(Long userId, Set<String> groupKeys);

    ProjectPermissionEvaluationContext buildActorContext(Long userId,
                                                         Set<String> groupKeys,
                                                         Long reporterUserId,
                                                         Long assigneeUserId);

    void checkRequiredPermissions(ProjectPermissionSubject subject,
                                  ProjectPermissionEvaluationContext actorContext,
                                  String... permissionKeys);

    void checkScheduleIssuesPermission(ProjectPermissionSubject subject,
                                       ProjectPermissionEvaluationContext actorContext);

    Long resolveAssigneeId(ProjectPermissionSubject subject,
                           Long requestedAssigneeId,
                           ProjectPermissionEvaluationContext actorContext);

    void checkSetIssueSecurityPermissionIfNeeded(ProjectPermissionSubject subject,
                                                 ProjectPermissionEvaluationContext actorContext,
                                                 Long requestedSecurityLevelId);
}
