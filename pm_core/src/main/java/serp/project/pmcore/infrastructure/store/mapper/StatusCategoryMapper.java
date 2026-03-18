/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.StatusCategoryEntity;
import serp.project.pmcore.infrastructure.store.model.StatusCategoryModel;

@Component
public class StatusCategoryMapper extends BaseMapper {

    public StatusCategoryModel toModel(StatusCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return StatusCategoryModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .key(entity.getKey())
                .colorName(entity.getColor())
                .isSystem(entity.getIsSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public StatusCategoryEntity toEntity(StatusCategoryModel model) {
        if (model == null) {
            return null;
        }
        return StatusCategoryEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .key(model.getKey())
                .color(model.getColorName())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }
}
