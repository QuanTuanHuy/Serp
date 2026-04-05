/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.service.handler;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.customfield.dto.CustomFieldResolutionContext;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

import java.util.ArrayList;
import java.util.List;

@Component
public class MultiSelectCustomFieldValueHandler extends AbstractWorkItemCustomFieldValueHandler {

    @Override
    public boolean supports(String normalizedTypeKey) {
        return "multiselect".equals(normalizedTypeKey);
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue) {
        List<?> values = asList(rawValue);
        List<WorkItemCustomFieldValueEntity> resolvedValues = new ArrayList<>();
        for (int index = 0; index < values.size(); index++) {
            CustomFieldOptionEntity option = resolveOption(values.get(index), context.options(), context.fieldKey());
            resolvedValues.add(buildCustomFieldValue(
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
                    index
            ));
        }
        return resolvedValues;
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveDefaults(CustomFieldResolutionContext context,
                                                                List<CustomFieldContextDefaultValueEntity> defaultValues) {
        if (defaultValues == null || defaultValues.isEmpty()) {
            return List.of();
        }

        List<WorkItemCustomFieldValueEntity> resolvedValues = new ArrayList<>();
        for (int index = 0; index < defaultValues.size(); index++) {
            CustomFieldContextDefaultValueEntity defaultValue = defaultValues.get(index);
            if (defaultValue.getOptionValueId() == null) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                        "Multiselect default must reference option value: field=" + context.fieldKey()
                );
            }

            CustomFieldOptionEntity option = resolveOption(defaultValue.getOptionValueId(), context.options(), context.fieldKey());
            resolvedValues.add(buildCustomFieldValue(
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
                    defaultValue.getSortOrder() == null ? index : defaultValue.getSortOrder()
            ));
        }
        return resolvedValues;
    }
}
