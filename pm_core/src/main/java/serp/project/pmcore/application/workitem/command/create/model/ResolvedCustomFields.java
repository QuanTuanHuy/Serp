/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.model;

import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;

import java.util.List;

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
