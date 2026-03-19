/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenTabEntity;
import serp.project.pmcore.infrastructure.store.model.ScreenTabModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScreenTabMapper extends BaseMapper {

    public ScreenTabEntity toEntity(ScreenTabModel model) {
        if (model == null) { return null; }
        return ScreenTabEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .screenId(model.getScreenId())
                .name(model.getName())
                .sequence(model.getSequence())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ScreenTabModel toModel(ScreenTabEntity entity) {
        if (entity == null) { return null; }
        return ScreenTabModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .screenId(entity.getScreenId())
                .name(entity.getName())
                .sequence(entity.getSequence())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ScreenTabEntity> toEntities(List<ScreenTabModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<ScreenTabModel> toModels(List<ScreenTabEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
