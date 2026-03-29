/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.infrastructure.store.model.WorkItemCustomFieldValueModel;

import java.util.Collections;
import java.util.List;

@Component
public class WorkItemCustomFieldValueMapper extends BaseMapper {

    public WorkItemCustomFieldValueModel toModel(WorkItemCustomFieldValueEntity entity) {
        if (entity == null) {
            return null;
        }
        return WorkItemCustomFieldValueModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workItemId(entity.getWorkItemId())
                .customFieldId(entity.getCustomFieldId())
                .customFieldContextId(entity.getCustomFieldContextId())
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

    public WorkItemCustomFieldValueEntity toEntity(WorkItemCustomFieldValueModel model) {
        if (model == null) {
            return null;
        }
        return WorkItemCustomFieldValueEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .workItemId(model.getWorkItemId())
                .customFieldId(model.getCustomFieldId())
                .customFieldContextId(model.getCustomFieldContextId())
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

    public List<WorkItemCustomFieldValueEntity> toEntities(List<WorkItemCustomFieldValueModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public List<WorkItemCustomFieldValueModel> toModels(List<WorkItemCustomFieldValueEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).toList();
    }
}
