/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.permission.entity.PermissionDefinitionEntity;
import serp.project.pmcore.infrastructure.store.model.PermissionDefinitionModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionDefinitionMapper extends BaseMapper {

    public PermissionDefinitionEntity toEntity(PermissionDefinitionModel model) {
        if (model == null) { return null; }
        return PermissionDefinitionEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .permissionKey(model.getPermissionKey())
                .name(model.getName())
                .description(model.getDescription())
                .category(model.getCategory())
                .isSystem(model.getIsSystem())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public List<PermissionDefinitionEntity> toEntities(List<PermissionDefinitionModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
