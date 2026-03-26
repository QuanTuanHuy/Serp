/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.workitem.create.handler;

import serp.project.pmcore.domain.dto.workitem.create.CustomFieldResolutionContext;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;

import java.util.List;

public interface IWorkItemCustomFieldValueHandler {

    boolean supports(String normalizedTypeKey);

    default boolean isFallback() {
        return false;
    }

    List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue);

    List<WorkItemCustomFieldValueEntity> resolveDefaults(CustomFieldResolutionContext context,
                                                         List<CustomFieldContextDefaultValueEntity> defaultValues);
}
