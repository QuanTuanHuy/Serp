/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.infrastructure.store.model.ModuleModel;

@Component
public class ModuleMapper extends BaseMapper {
    
    public ModuleEntity toEntity(ModuleModel model) {
        if (model == null) {
            return null;
        }
        
        return ModuleEntity.builder()
                .id(model.getId())
                .moduleName(model.getModuleName())
                .description(model.getDescription())
                .keycloakClientId(model.getKeycloakClientId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public ModuleModel toModel(ModuleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return ModuleModel.builder()
                .id(entity.getId())
                .moduleName(entity.getModuleName())
                .description(entity.getDescription())
                .keycloakClientId(entity.getKeycloakClientId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<ModuleEntity> toEntityList(List<ModuleModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<ModuleModel> toModelList(List<ModuleEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public ModuleEntity createModuleMapper(CreateModuleDto request) {
        if (request == null) {
            return null;
        }

        return ModuleEntity.builder()
                .moduleName(request.getName())
                .description(request.getDescription())
                .keycloakClientId(request.getKeycloakClientId())
                .build();
    }

    public ModuleEntity updateModuleMapper(ModuleEntity entity, UpdateModuleDto request) {
        if (entity == null || request == null) {
            return entity;
        }

        entity.setModuleName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setKeycloakClientId(request.getKeycloakClientId());
        return entity;
    }
}
