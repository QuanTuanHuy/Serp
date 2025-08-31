/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.UserRoleEntity;
import serp.project.account.infrastructure.store.model.UserRoleModel;

@Component
public class UserRoleMapper extends BaseMapper {
    
    public UserRoleEntity toEntity(UserRoleModel model) {
        if (model == null) {
            return null;
        }
        
        return UserRoleEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .roleId(model.getRoleId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public UserRoleModel toModel(UserRoleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return UserRoleModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .roleId(entity.getRoleId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<UserRoleEntity> toEntityList(List<UserRoleModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<UserRoleModel> toModelList(List<UserRoleEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
