/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.infrastructure.store.model.PrioritySchemeModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrioritySchemeMapper extends BaseMapper {

    public PrioritySchemeEntity toEntity(PrioritySchemeModel model) {
        if (model == null) { return null; }
        return PrioritySchemeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .defaultPriorityId(model.getDefaultPriorityId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public PrioritySchemeModel toModel(PrioritySchemeEntity entity) {
        if (entity == null) { return null; }
        return PrioritySchemeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultPriorityId(entity.getDefaultPriorityId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<PrioritySchemeEntity> toEntities(List<PrioritySchemeModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
