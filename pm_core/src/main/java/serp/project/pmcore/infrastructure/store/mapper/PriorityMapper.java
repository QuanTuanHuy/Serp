/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.PriorityEntity;
import serp.project.pmcore.infrastructure.store.model.PriorityModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PriorityMapper extends BaseMapper {

    public PriorityEntity toEntity(PriorityModel model) {
        if (model == null) { return null; }
        return PriorityEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .iconUrl(model.getIconUrl())
                .color(model.getColor())
                .sequence(model.getSequence())
                .isSystem(Boolean.TRUE.equals(model.getIsSystem()))
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public PriorityModel toModel(PriorityEntity entity) {
        if (entity == null) { return null; }
        return PriorityModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .iconUrl(entity.getIconUrl())
                .color(entity.getColor())
                .sequence(entity.getSequence())
                .isSystem(entity.isSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<PriorityEntity> toEntities(List<PriorityModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
