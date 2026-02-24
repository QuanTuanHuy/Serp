/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.infrastructure.store.model.PrioritySchemeItemModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrioritySchemeItemMapper extends BaseMapper {

    public PrioritySchemeItemEntity toEntity(PrioritySchemeItemModel model) {
        if (model == null) { return null; }
        return PrioritySchemeItemEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeId(model.getSchemeId())
                .priorityId(model.getPriorityId())
                .sequence(model.getSequence())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public PrioritySchemeItemModel toModel(PrioritySchemeItemEntity entity) {
        if (entity == null) { return null; }
        return PrioritySchemeItemModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeId(entity.getSchemeId())
                .priorityId(entity.getPriorityId())
                .sequence(entity.getSequence())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<PrioritySchemeItemEntity> toEntities(List<PrioritySchemeItemModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<PrioritySchemeItemModel> toModels(List<PrioritySchemeItemEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
