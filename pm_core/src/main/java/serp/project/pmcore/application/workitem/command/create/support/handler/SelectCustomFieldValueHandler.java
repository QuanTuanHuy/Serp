/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support.handler;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.create.model.CustomFieldResolutionContext;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;

import java.util.List;

@Component
public class SelectCustomFieldValueHandler extends AbstractWorkItemCustomFieldValueHandler {

    @Override
    public boolean supports(String normalizedTypeKey) {
        return "select".equals(normalizedTypeKey);
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue) {
        CustomFieldOptionEntity option = resolveOption(rawValue, context.options(), context.fieldKey());
        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_OPTION,
                null,
                null,
                null,
                null,
                null,
                null,
                option.getId(),
                null,
                0
        ));
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveDefaults(CustomFieldResolutionContext context,
                                                                List<CustomFieldContextDefaultValueEntity> defaultValues) {
        CustomFieldContextDefaultValueEntity defaultValue = requireSingleDefaultValue(defaultValues, context.fieldKey());
        if (defaultValue == null || defaultValue.getOptionValueId() == null) {
            return List.of();
        }

        CustomFieldOptionEntity option = resolveOption(defaultValue.getOptionValueId(), context.options(), context.fieldKey());
        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_OPTION,
                null,
                null,
                null,
                null,
                null,
                null,
                option.getId(),
                null,
                0
        ));
    }
}
