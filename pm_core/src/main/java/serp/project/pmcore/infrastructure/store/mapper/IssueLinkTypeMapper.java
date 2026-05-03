/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;
import serp.project.pmcore.infrastructure.store.model.IssueLinkTypeModel;

import java.util.Collections;
import java.util.List;

@Component
public class IssueLinkTypeMapper extends BaseMapper {

    public IssueLinkTypeModel toModel(IssueLinkTypeEntity entity) {
        if (entity == null) {
            return null;
        }
        return IssueLinkTypeModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .outwardDescription(entity.getOutwardDescription())
                .inwardDescription(entity.getInwardDescription())
                .isSystem(entity.getIsSystem())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt()))
                .build();
    }

    public IssueLinkTypeEntity toEntity(IssueLinkTypeModel model) {
        if (model == null) {
            return null;
        }
        return IssueLinkTypeEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .outwardDescription(model.getOutwardDescription())
                .inwardDescription(model.getInwardDescription())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt()))
                .build();
    }

    public List<IssueLinkTypeEntity> toEntities(List<IssueLinkTypeModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(this::toEntity).toList();
    }
}
