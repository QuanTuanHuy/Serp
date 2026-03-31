/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public interface IWorkItemDeleteAuthorizationService {
    void checkDeletePermission(ProjectEntity project,
                               ProjectPermissionEvaluationContext actorContext);

    void checkDeleteSecurityAccess(ProjectEntity project,
                                   WorkItemEntity workItem,
                                   ProjectPermissionEvaluationContext actorContext);
}
