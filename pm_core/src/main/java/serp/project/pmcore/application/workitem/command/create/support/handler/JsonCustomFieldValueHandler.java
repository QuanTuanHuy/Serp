/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support.handler;

import org.springframework.stereotype.Component;
import serp.project.pmcore.application.workitem.command.create.internal.CustomFieldResolutionContext;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;

@Component
public class JsonCustomFieldValueHandler extends AbstractWorkItemCustomFieldValueHandler {

    private final JsonUtils jsonUtils;

    public JsonCustomFieldValueHandler(JsonUtils jsonUtils) {
        this.jsonUtils = jsonUtils;
    }

    @Override
    public boolean supports(String normalizedTypeKey) {
        return false;
    }

    @Override
    public boolean isFallback() {
        return true;
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveProvided(CustomFieldResolutionContext context, Object rawValue) {
        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_JSON,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                jsonUtils.toJson(rawValue),
                0
        ));
    }

    @Override
    public List<WorkItemCustomFieldValueEntity> resolveDefaults(CustomFieldResolutionContext context,
                                                                List<CustomFieldContextDefaultValueEntity> defaultValues) {
        CustomFieldContextDefaultValueEntity defaultValue = requireSingleDefaultValue(defaultValues, context.fieldKey());
        String jsonValue = extractFallbackJsonValue(defaultValue);
        if (jsonValue == null) {
            return List.of();
        }

        return List.of(buildCustomFieldValue(
                context,
                VALUE_TYPE_JSON,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                jsonValue,
                0
        ));
    }

    private String extractFallbackJsonValue(CustomFieldContextDefaultValueEntity defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (defaultValue.getJsonValue() != null) {
            return defaultValue.getJsonValue();
        }
        if (defaultValue.getTextValue() != null) {
            return jsonUtils.toJson(defaultValue.getTextValue());
        }
        if (defaultValue.getNumberValue() != null) {
            return jsonUtils.toJson(defaultValue.getNumberValue());
        }
        if (defaultValue.getDateValue() != null) {
            return jsonUtils.toJson(defaultValue.getDateValue());
        }
        if (defaultValue.getDatetimeValue() != null) {
            return jsonUtils.toJson(defaultValue.getDatetimeValue());
        }
        if (defaultValue.getUserValueId() != null) {
            return jsonUtils.toJson(defaultValue.getUserValueId());
        }
        if (defaultValue.getGroupValueId() != null) {
            return jsonUtils.toJson(defaultValue.getGroupValueId());
        }
        if (defaultValue.getOptionValueId() != null) {
            return jsonUtils.toJson(defaultValue.getOptionValueId());
        }
        return null;
    }
}
