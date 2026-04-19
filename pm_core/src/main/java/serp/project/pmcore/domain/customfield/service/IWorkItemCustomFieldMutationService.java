/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.service;

import serp.project.pmcore.domain.customfield.dto.WorkItemCustomFieldMutationPlan;

import java.util.Map;

public interface IWorkItemCustomFieldMutationService {
    WorkItemCustomFieldMutationPlan planCreate(String issueTypeKey,
                                               Map<String, Object> requestCustomFields,
                                               Map<String, Boolean> requiredByFieldKey);

    WorkItemCustomFieldMutationPlan planUpdate(String issueTypeKey,
                                               Long workItemId,
                                               Long tenantId,
                                               Map<String, Object> requestCustomFields,
                                               Map<String, Boolean> requiredByFieldKey);

    void applyPlan(Long workItemId,
                   Long tenantId,
                   Long userId,
                   WorkItemCustomFieldMutationPlan plan);
}
