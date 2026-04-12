/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.infrastructure.store.model.FieldConfigSchemeModel;

@Component
public class FieldConfigSchemeMapper extends BaseMapper {

    public FieldConfigSchemeEntity toEntity(FieldConfigSchemeModel model) {
        if (model == null) { return null; }
        return FieldConfigSchemeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .defaultFieldConfigId(model.getDefaultFieldConfigId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public FieldConfigSchemeModel toModel(FieldConfigSchemeEntity entity) {
        if (entity == null) { return null; }
        return FieldConfigSchemeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .defaultFieldConfigId(entity.getDefaultFieldConfigId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
