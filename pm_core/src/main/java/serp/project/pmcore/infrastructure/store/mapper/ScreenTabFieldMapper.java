/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenTabFieldEntity;
import serp.project.pmcore.infrastructure.store.model.ScreenTabFieldModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScreenTabFieldMapper extends BaseMapper {

    public ScreenTabFieldEntity toEntity(ScreenTabFieldModel model) {
        if (model == null) { return null; }
        return ScreenTabFieldEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .screenTabId(model.getScreenTabId())
                .fieldRefType(model.getFieldRefType())
                .fieldRef(model.getFieldRef())
                .sequence(model.getSequence())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ScreenTabFieldModel toModel(ScreenTabFieldEntity entity) {
        if (entity == null) { return null; }
        return ScreenTabFieldModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .screenTabId(entity.getScreenTabId())
                .fieldRefType(entity.getFieldRefType())
                .fieldRef(entity.getFieldRef())
                .sequence(entity.getSequence())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<ScreenTabFieldEntity> toEntities(List<ScreenTabFieldModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<ScreenTabFieldModel> toModels(List<ScreenTabFieldEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
