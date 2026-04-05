/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public interface IWorkItemTransitionAuthorizationService {
    void checkTransitionPermissions(ProjectEntity project,
                                   ProjectPermissionEvaluationContext actorContext);

    void checkFieldLevelPermissions(ProjectEntity project,
                                    ProjectPermissionEvaluationContext actorContext,
                                    TransitionWorkItemStatusData data);

    Long resolveAssigneeId(ProjectEntity project,
                            ProjectPermissionEvaluationContext actorContext,
                            WorkItemEntity workItem,
                            TransitionWorkItemStatusData data);

    Long resolveSecurityLevelId(ProjectEntity project,
                                WorkItemEntity workItem,
                                TransitionWorkItemStatusData data,
                                Long tenantId);


    void checkIssueSecurityAccessIfNeeded(ProjectEntity project,
                                          WorkItemEntity workItem,
                                          ProjectPermissionEvaluationContext actorContext,
                                          Long tenantId);
}
