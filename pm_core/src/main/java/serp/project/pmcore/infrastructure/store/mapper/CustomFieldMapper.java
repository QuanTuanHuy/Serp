/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.infrastructure.store.model.CustomFieldModel;

import java.util.Collections;
import java.util.List;

@Component
public class CustomFieldMapper extends BaseMapper {

    public CustomFieldEntity toEntity(CustomFieldModel model) {
        if (model == null) {
            return null;
        }
        return CustomFieldEntity.builder()
                .id(model.getId())
                .fieldKey(model.getFieldKey())
                .name(model.getName())
                .description(model.getDescription())
                .typeKey(model.getTypeKey())
                .searchTemplate(model.getSearchTemplate())
                .isSystem(model.getIsSystem())
                .schemaJson(model.getSchemaJson())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<CustomFieldEntity> toEntities(List<CustomFieldModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
