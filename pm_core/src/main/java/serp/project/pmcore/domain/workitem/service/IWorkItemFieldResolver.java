/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;

public interface IWorkItemFieldResolver {
    WorkItemFieldRules resolveFieldRules(Long projectId,
                                         Long fieldConfigSchemeId,
                                         Long issueTypeId,
                                         Long screenId,
                                         Long tenantId);
}
