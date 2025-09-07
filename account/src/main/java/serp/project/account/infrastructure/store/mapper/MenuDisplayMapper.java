/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.MenuDisplayEntity;
import serp.project.account.infrastructure.store.model.MenuDisplayModel;

@Component
public class MenuDisplayMapper extends BaseMapper {
    
    public MenuDisplayEntity toEntity(MenuDisplayModel model) {
        if (model == null) {
            return null;
        }
        
        return MenuDisplayEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .path(model.getPath())
                .icon(model.getIcon())
                .order(model.getOrder())
                .parentId(model.getParentId())
                .moduleId(model.getModuleId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public MenuDisplayModel toModel(MenuDisplayEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return MenuDisplayModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .path(entity.getPath())
                .icon(entity.getIcon())
                .order(entity.getOrder())
                .parentId(entity.getParentId())
                .moduleId(entity.getModuleId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<MenuDisplayEntity> toEntityList(List<MenuDisplayModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<MenuDisplayModel> toModelList(List<MenuDisplayEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
