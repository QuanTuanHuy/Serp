/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;

import java.util.Set;

public interface IWorkItemAuthorizationSupportService {
    ProjectPermissionEvaluationContext buildActorContext(Long userId, Set<String> groupKeys);

    ProjectPermissionEvaluationContext buildActorContext(Long userId,
                                                         Set<String> groupKeys,
                                                         Long reporterUserId,
                                                         Long assigneeUserId);

    void checkRequiredPermissions(ProjectEntity project,
                                  ProjectPermissionEvaluationContext actorContext,
                                  String... permissionKeys);

    void checkScheduleIssuesPermissionIfNeeded(ProjectEntity project,
                                              ProjectPermissionEvaluationContext actorContext,
                                              Long dueDate);

    Long resolveAssigneeId(ProjectEntity project,
                          Long requestedAssigneeId,
                          ProjectPermissionEvaluationContext actorContext);

    void checkSetIssueSecurityPermissionIfNeeded(ProjectEntity project,
                                                ProjectPermissionEvaluationContext actorContext,
                                                Long requestedSecurityLevelId);
}
