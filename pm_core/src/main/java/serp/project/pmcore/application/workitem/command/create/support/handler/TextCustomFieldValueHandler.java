/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support.handler;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.create.internal.CustomFieldResolutionContext;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

import java.util.List;

@Component
public class TextCustomFieldValueHandler extends AbstractWorkItemCustomFieldValueHandler {

    @Override
    public boolean supports(String normalizedTypeKey) {
        return "text".equals(normalizedTypeKey) || "url".equals(normalizedTypeKey);
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue) {
        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_TEXT,
                requireTextValue(rawValue, context.fieldKey()),
                null,
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
        if (defaultValue == null || defaultValue.getTextValue() == null) {
            return List.of();
        }

        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_TEXT,
                defaultValue.getTextValue(),
                null,
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
