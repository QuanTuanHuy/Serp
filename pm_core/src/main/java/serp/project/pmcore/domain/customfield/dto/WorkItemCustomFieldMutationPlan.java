/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.dto;

import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

import java.util.List;

public record WorkItemCustomFieldMutationPlan(
        List<WorkItemCustomFieldValueEntity> resolvedValues,
        List<String> missingRequiredFields,
        List<Long> customFieldIdsToReplace,
        List<String> changedFieldKeys
) {

    public WorkItemCustomFieldMutationPlan {
        resolvedValues = List.copyOf(resolvedValues);
        missingRequiredFields = List.copyOf(missingRequiredFields);
        customFieldIdsToReplace = List.copyOf(customFieldIdsToReplace);
        changedFieldKeys = List.copyOf(changedFieldKeys);
    }

    public static WorkItemCustomFieldMutationPlan empty() {
        return new WorkItemCustomFieldMutationPlan(List.of(), List.of(), List.of(), List.of());
    }
}
