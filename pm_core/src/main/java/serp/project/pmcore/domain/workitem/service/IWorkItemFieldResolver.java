/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;

public interface IWorkItemFieldResolver {
    WorkItemFieldRules resolveFieldRules(ProjectEntity project,
                                         Long issueTypeId,
                                         Long screenId,
                                         Long tenantId);
}
