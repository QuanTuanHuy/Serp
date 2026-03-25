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
}
