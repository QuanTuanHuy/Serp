/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextDefaultValueModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomFieldContextDefaultValueMapper extends BaseMapper {

    public CustomFieldContextDefaultValueEntity toEntity(CustomFieldContextDefaultValueModel model) {
        if (model == null) {
            return null;
        }
        return CustomFieldContextDefaultValueEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .contextId(model.getContextId())
                .valueType(model.getValueType())
                .textValue(model.getTextValue())
                .numberValue(model.getNumberValue())
                .dateValue(localDateToLong(model.getDateValue()))
                .datetimeValue(localDateTimeToLong(model.getDatetimeValue()))
                .userValueId(model.getUserValueId())
                .groupValueId(model.getGroupValueId())
                .optionValueId(model.getOptionValueId())
                .jsonValue(model.getJsonValue())
                .sortOrder(model.getSortOrder())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<CustomFieldContextDefaultValueEntity> toEntities(List<CustomFieldContextDefaultValueModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public CustomFieldContextDefaultValueModel toModel(CustomFieldContextDefaultValueEntity entity) {
        if (entity == null) {
            return null;
        }
        return CustomFieldContextDefaultValueModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .contextId(entity.getContextId())
                .valueType(entity.getValueType())
                .textValue(entity.getTextValue())
                .numberValue(entity.getNumberValue())
                .dateValue(longToLocalDate(entity.getDateValue()))
                .datetimeValue(longToLocalDateTime(entity.getDatetimeValue()))
                .userValueId(entity.getUserValueId())
                .groupValueId(entity.getGroupValueId())
                .optionValueId(entity.getOptionValueId())
                .jsonValue(entity.getJsonValue())
                .sortOrder(entity.getSortOrder())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<CustomFieldContextDefaultValueModel> toModels(List<CustomFieldContextDefaultValueEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
