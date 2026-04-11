/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.service.handler;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.customfield.dto.CustomFieldResolutionContext;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

import java.util.List;

@Component
public class NumberCustomFieldValueHandler extends AbstractWorkItemCustomFieldValueHandler {

    @Override
    public boolean supports(String normalizedTypeKey) {
        return "number".equals(normalizedTypeKey);
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue) {
        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_NUMBER,
                null,
                requireNumberValue(rawValue, context.fieldKey()),
                null,
                null,
                null,
                null,
                null,
                null,
                0
        ));
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveDefaults(CustomFieldResolutionContext context,
                                                                List<CustomFieldContextDefaultValueEntity> defaultValues) {
        CustomFieldContextDefaultValueEntity defaultValue = requireSingleDefaultValue(defaultValues, context.fieldKey());
        if (defaultValue == null || defaultValue.getNumberValue() == null) {
            return List.of();
        }

        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_NUMBER,
                null,
                defaultValue.getNumberValue(),
                null,
                null,
                null,
                null,
                null,
                null,
                0
        ));
    }
}
