/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.StatusEntity;
import serp.project.pmcore.infrastructure.store.model.StatusModel;

import java.util.Collections;
import java.util.List;

@Component
public class StatusMapper extends BaseMapper {

    public StatusModel toModel(StatusEntity entity) {
        if (entity == null) {
            return null;
        }
        return StatusModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .statusKey(entity.getStatusKey())
                .name(entity.getName())
                .description(entity.getDescription())
                .iconUrl(entity.getIconUrl())
                .categoryId(entity.getCategoryId())
                .isSystem(entity.getIsSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public StatusEntity toEntity(StatusModel model) {
        if (model == null) {
            return null;
        }
        return StatusEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .statusKey(model.getStatusKey())
                .name(model.getName())
                .description(model.getDescription())
                .iconUrl(model.getIconUrl())
                .categoryId(model.getCategoryId())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<StatusEntity> toEntities(List<StatusModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
