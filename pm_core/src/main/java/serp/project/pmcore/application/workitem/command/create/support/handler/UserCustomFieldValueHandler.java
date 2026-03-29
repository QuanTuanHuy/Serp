/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support.handler;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.create.model.CustomFieldResolutionContext;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

import java.util.List;

@Component
public class UserCustomFieldValueHandler extends AbstractWorkItemCustomFieldValueHandler {

    @Override
    public boolean supports(String normalizedTypeKey) {
        return "user".equals(normalizedTypeKey);
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue) {
        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_USER,
                null,
                null,
                null,
                null,
                requireUserValue(rawValue, context.fieldKey()),
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
        if (defaultValue == null || defaultValue.getUserValueId() == null) {
            return List.of();
        }

        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_USER,
                null,
                null,
                null,
                null,
                defaultValue.getUserValueId(),
                null,
                null,
                null,
                0
        ));
    }
}
