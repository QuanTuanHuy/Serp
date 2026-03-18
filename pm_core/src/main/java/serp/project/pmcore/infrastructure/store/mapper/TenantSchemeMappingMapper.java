/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.infrastructure.store.model.TenantSchemeMappingModel;

@Component
public class TenantSchemeMappingMapper extends BaseMapper {

    public TenantSchemeMappingEntity toEntity(TenantSchemeMappingModel model) {
        if (model == null) {
            return null;
        }
        return TenantSchemeMappingEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeType(model.getSchemeType())
                .sourceSchemeId(model.getSourceSchemeId())
                .tenantSchemeId(model.getTenantSchemeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public TenantSchemeMappingModel toModel(TenantSchemeMappingEntity entity) {
        if (entity == null) {
            return null;
        }
        return TenantSchemeMappingModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeType(entity.getSchemeType())
                .sourceSchemeId(entity.getSourceSchemeId())
                .tenantSchemeId(entity.getTenantSchemeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
