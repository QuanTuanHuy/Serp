/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.infrastructure.store.model.CustomFieldOptionModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomFieldOptionMapper extends BaseMapper {

    public CustomFieldOptionEntity toEntity(CustomFieldOptionModel model) {
        if (model == null) {
            return null;
        }
        return CustomFieldOptionEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .customFieldContextId(model.getCustomFieldContextId())
                .optionKey(model.getOptionKey())
                .value(model.getValue())
                .sequence(model.getSequence())
                .parentOptionId(model.getParentOptionId())
                .isDisabled(model.getIsDisabled())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<CustomFieldOptionEntity> toEntities(List<CustomFieldOptionModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }

    public CustomFieldOptionModel toModel(CustomFieldOptionEntity entity) {
        if (entity == null) {
            return null;
        }
        return CustomFieldOptionModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .customFieldContextId(entity.getCustomFieldContextId())
                .optionKey(entity.getOptionKey())
                .value(entity.getValue())
                .sequence(entity.getSequence())
                .parentOptionId(entity.getParentOptionId())
                .isDisabled(entity.getIsDisabled())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<CustomFieldOptionModel> toModels(List<CustomFieldOptionEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
