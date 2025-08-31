/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.infrastructure.store.model.RoleModel;

@Component
public class RoleMapper extends BaseMapper {
    
    public RoleEntity toEntity(RoleModel model) {
        if (model == null) {
            return null;
        }
        
        return RoleEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public RoleModel toModel(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return RoleModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<RoleEntity> toEntityList(List<RoleModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<RoleModel> toModelList(List<RoleEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
