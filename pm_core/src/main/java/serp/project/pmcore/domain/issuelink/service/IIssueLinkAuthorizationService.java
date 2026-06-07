/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.service;

import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;

public interface IIssueLinkAuthorizationService {
    void checkReadAccess(ProjectEntity project,
                         WorkItemEntity workItem,
                         ProjectPermissionEvaluationContext actorContext);

    void checkWriteAccess(ProjectEntity project,
                          WorkItemEntity workItem,
                          ProjectPermissionEvaluationContext actorContext);
}
