/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support.handler;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.create.model.CustomFieldResolutionContext;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;

import java.util.List;

@Component
public class GroupCustomFieldValueHandler extends AbstractWorkItemCustomFieldValueHandler {

    @Override
    public boolean supports(String normalizedTypeKey) {
        return "group".equals(normalizedTypeKey);
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue) {
        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_GROUP,
                null,
                null,
                null,
                null,
                null,
                requireGroupValue(rawValue, context.fieldKey()),
                null,
                null,
                0
        ));
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveDefaults(CustomFieldResolutionContext context,
                                                                List<CustomFieldContextDefaultValueEntity> defaultValues) {
        CustomFieldContextDefaultValueEntity defaultValue = requireSingleDefaultValue(defaultValues, context.fieldKey());
        if (defaultValue == null || defaultValue.getGroupValueId() == null) {
            return List.of();
        }

        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_GROUP,
                null,
                null,
                null,
                null,
                null,
                defaultValue.getGroupValueId(),
                null,
                null,
                0
        ));
    }
}
