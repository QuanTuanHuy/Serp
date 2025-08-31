/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.PermissionEntity;
import serp.project.account.infrastructure.store.model.PermissionModel;

@Component
public class PermissionMapper extends BaseMapper {
    
    public PermissionEntity toEntity(PermissionModel model) {
        if (model == null) {
            return null;
        }
        
        return PermissionEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public PermissionModel toModel(PermissionEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return PermissionModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<PermissionEntity> toEntityList(List<PermissionModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<PermissionModel> toModelList(List<PermissionEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
