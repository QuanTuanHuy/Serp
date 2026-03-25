/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenSchemeItemEntity;
import serp.project.pmcore.infrastructure.store.model.ScreenSchemeItemModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScreenSchemeItemMapper extends BaseMapper {

    public ScreenSchemeItemEntity toEntity(ScreenSchemeItemModel model) {
        if (model == null) { return null; }
        return ScreenSchemeItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .screenSchemeId(model.getScreenSchemeId())
                .operationKey(model.getOperationKey())
                .screenId(model.getScreenId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ScreenSchemeItemModel toModel(ScreenSchemeItemEntity entity) {
        if (entity == null) { return null; }
        return ScreenSchemeItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .screenSchemeId(entity.getScreenSchemeId())
                .operationKey(entity.getOperationKey())
                .screenId(entity.getScreenId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ScreenSchemeItemEntity> toEntities(List<ScreenSchemeItemModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<ScreenSchemeItemModel> toModels(List<ScreenSchemeItemEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
