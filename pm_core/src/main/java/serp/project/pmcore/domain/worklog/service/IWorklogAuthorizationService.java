/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.worklog.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.worklog.entity.WorklogEntity;

public interface IWorklogAuthorizationService {
    void checkReadAccess(ProjectEntity project,
                         WorkItemEntity workItem,
                         ProjectPermissionEvaluationContext actorContext);

    void checkCreateAccess(ProjectEntity project,
                           WorkItemEntity workItem,
                           ProjectPermissionEvaluationContext actorContext);

    void checkUpdateAccess(ProjectEntity project,
                           WorkItemEntity workItem,
                           WorklogEntity worklog,
                           ProjectPermissionEvaluationContext actorContext);

    void checkDeleteAccess(ProjectEntity project,
                           WorkItemEntity workItem,
                           WorklogEntity worklog,
                           ProjectPermissionEvaluationContext actorContext);
}
