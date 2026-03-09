/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.LabelEntity;
import serp.project.pmcore.infrastructure.store.model.LabelModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class LabelMapper extends BaseMapper {

    public LabelEntity toEntity(LabelModel model) {
        if (model == null) { return null; }
        return LabelEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .projectId(model.getProjectId())
                .name(model.getName())
                .color(model.getColor())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public LabelModel toModel(LabelEntity entity) {
        if (entity == null) { return null; }
        return LabelModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .projectId(entity.getProjectId())
                .name(entity.getName())
                .color(entity.getColor())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<LabelEntity> toEntities(List<LabelModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
