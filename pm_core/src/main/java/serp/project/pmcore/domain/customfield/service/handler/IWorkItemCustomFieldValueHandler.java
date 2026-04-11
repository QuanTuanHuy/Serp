/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.service.handler;

import serp.project.pmcore.domain.customfield.dto.CustomFieldResolutionContext;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

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
