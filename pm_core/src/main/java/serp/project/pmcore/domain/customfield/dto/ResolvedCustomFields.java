/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.dto;

import java.util.List;

import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

public record ResolvedCustomFields(List<WorkItemCustomFieldValueEntity> values,
                                   List<String> missingFields) {

    public ResolvedCustomFields {
        values = List.copyOf(values);
        missingFields = List.copyOf(missingFields);
    }

    public static ResolvedCustomFields empty() {
        return new ResolvedCustomFields(List.of(), List.of());
    }
}
