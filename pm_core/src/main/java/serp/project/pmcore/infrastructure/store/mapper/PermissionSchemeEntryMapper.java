/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntryEntity;
import serp.project.pmcore.infrastructure.store.model.PermissionSchemeEntryModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionSchemeEntryMapper extends BaseMapper {

    public PermissionSchemeEntryEntity toEntity(PermissionSchemeEntryModel model) {
        if (model == null) { return null; }
        return PermissionSchemeEntryEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .schemeId(model.getSchemeId())
                .permissionKey(model.getPermissionKey())
                .granteeType(model.getGranteeType())
                .granteeRef(model.getGranteeRef())
                .customFieldId(model.getCustomFieldId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public PermissionSchemeEntryModel toModel(PermissionSchemeEntryEntity entity) {
        if (entity == null) { return null; }
        return PermissionSchemeEntryModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .schemeId(entity.getSchemeId())
                .permissionKey(entity.getPermissionKey())
                .granteeType(entity.getGranteeType())
                .granteeRef(entity.getGranteeRef())
                .customFieldId(entity.getCustomFieldId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<PermissionSchemeEntryEntity> toEntities(List<PermissionSchemeEntryModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<PermissionSchemeEntryModel> toModels(List<PermissionSchemeEntryEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
