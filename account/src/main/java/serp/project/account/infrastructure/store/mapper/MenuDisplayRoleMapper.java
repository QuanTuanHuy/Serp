/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.MenuDisplayRoleEntity;
import serp.project.account.infrastructure.store.model.MenuDisplayRoleModel;

@Component
public class MenuDisplayRoleMapper extends BaseMapper {
    
    public MenuDisplayRoleEntity toEntity(MenuDisplayRoleModel model) {
        if (model == null) {
            return null;
        }
        
        return MenuDisplayRoleEntity.builder()
                .id(model.getId())
                .roleId(model.getRoleId())
                .menuDisplayId(model.getMenuDisplayId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public MenuDisplayRoleModel toModel(MenuDisplayRoleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return MenuDisplayRoleModel.builder()
                .id(entity.getId())
                .roleId(entity.getRoleId())
                .menuDisplayId(entity.getMenuDisplayId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<MenuDisplayRoleEntity> toEntityList(List<MenuDisplayRoleModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<MenuDisplayRoleModel> toModelList(List<MenuDisplayRoleEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
