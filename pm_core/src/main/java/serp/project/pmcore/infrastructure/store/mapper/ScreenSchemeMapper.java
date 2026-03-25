/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.ScreenSchemeEntity;
import serp.project.pmcore.infrastructure.store.model.ScreenSchemeModel;

@Component
public class ScreenSchemeMapper extends BaseMapper {

    public ScreenSchemeEntity toEntity(ScreenSchemeModel model) {
        if (model == null) { return null; }
        return ScreenSchemeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .defaultScreenId(model.getDefaultScreenId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public ScreenSchemeModel toModel(ScreenSchemeEntity entity) {
        if (entity == null) { return null; }
        return ScreenSchemeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultScreenId(entity.getDefaultScreenId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
