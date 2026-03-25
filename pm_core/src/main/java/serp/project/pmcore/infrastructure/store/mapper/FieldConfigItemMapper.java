/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.FieldConfigItemEntity;
import serp.project.pmcore.infrastructure.store.model.FieldConfigItemModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FieldConfigItemMapper extends BaseMapper {

    public FieldConfigItemEntity toEntity(FieldConfigItemModel model) {
        if (model == null) { return null; }
        return FieldConfigItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .fieldConfigId(model.getFieldConfigId())
                .fieldRefType(model.getFieldRefType())
                .fieldRef(model.getFieldRef())
                .isRequired(model.getIsRequired())
                .isHidden(model.getIsHidden())
                .rendererKey(model.getRendererKey())
                .sequence(model.getSequence())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public FieldConfigItemModel toModel(FieldConfigItemEntity entity) {
        if (entity == null) { return null; }
        return FieldConfigItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .fieldConfigId(entity.getFieldConfigId())
                .fieldRefType(entity.getFieldRefType())
                .fieldRef(entity.getFieldRef())
                .isRequired(entity.getIsRequired())
                .isHidden(entity.getIsHidden())
                .rendererKey(entity.getRendererKey())
                .sequence(entity.getSequence())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<FieldConfigItemEntity> toEntities(List<FieldConfigItemModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<FieldConfigItemModel> toModels(List<FieldConfigItemEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
