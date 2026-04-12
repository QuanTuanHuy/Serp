/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.entity.TenantSchemeDefaultEntity;
import serp.project.pmcore.infrastructure.store.model.TenantSchemeDefaultModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TenantSchemeDefaultMapper extends BaseMapper {

    public TenantSchemeDefaultEntity toEntity(TenantSchemeDefaultModel model) {
        if (model == null) {
            return null;
        }

        return TenantSchemeDefaultEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeType(model.getSchemeType())
                .schemeId(model.getSchemeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public TenantSchemeDefaultModel toModel(TenantSchemeDefaultEntity entity) {
        if (entity == null) {
            return null;
        }

        return TenantSchemeDefaultModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeType(entity.getSchemeType())
                .schemeId(entity.getSchemeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<TenantSchemeDefaultEntity> toEntities(List<TenantSchemeDefaultModel> models) {
        if (models == null) {
            return Collections.emptyList();
        }

        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
