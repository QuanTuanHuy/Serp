/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigEntity;
import serp.project.pmcore.infrastructure.store.model.FieldConfigModel;

@Component
public class FieldConfigMapper extends BaseMapper {

    public FieldConfigEntity toEntity(FieldConfigModel model) {
        if (model == null) { return null; }
        return FieldConfigEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public FieldConfigModel toModel(FieldConfigEntity entity) {
        if (entity == null) { return null; }
        return FieldConfigModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isSystem(entity.getIsSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
