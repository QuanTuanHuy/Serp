/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.RolePermissionEntity;
import serp.project.account.infrastructure.store.model.RolePermissionModel;

@Component
public class RolePermissionMapper extends BaseMapper {
    
    public RolePermissionEntity toEntity(RolePermissionModel model) {
        if (model == null) {
            return null;
        }
        
        return RolePermissionEntity.builder()
                .id(model.getId())
                .roleId(model.getRoleId())
                .permissionId(model.getPermissionId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public RolePermissionModel toModel(RolePermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return RolePermissionModel.builder()
                .id(entity.getId())
                .roleId(entity.getRoleId())
                .permissionId(entity.getPermissionId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<RolePermissionEntity> toEntityList(List<RolePermissionModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<RolePermissionModel> toModelList(List<RolePermissionEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
