/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.UserGroupEntity;
import serp.project.account.infrastructure.store.model.UserGroupModel;

@Component
public class UserGroupMapper extends BaseMapper {
    
    public UserGroupEntity toEntity(UserGroupModel model) {
        if (model == null) {
            return null;
        }
        
        return UserGroupEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .groupId(model.getGroupId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public UserGroupModel toModel(UserGroupEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return UserGroupModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .groupId(entity.getGroupId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<UserGroupEntity> toEntityList(List<UserGroupModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<UserGroupModel> toModelList(List<UserGroupEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
