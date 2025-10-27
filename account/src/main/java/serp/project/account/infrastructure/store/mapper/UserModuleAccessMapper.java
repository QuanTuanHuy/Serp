/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.response.UserModuleAccessResponse;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.entity.UserModuleAccessEntity;
import serp.project.account.infrastructure.store.model.UserModuleAccessModel;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserModuleAccessMapper extends BaseMapper {

    public UserModuleAccessEntity toEntity(UserModuleAccessModel model) {
        if (model == null) {
            return null;
        }

        return UserModuleAccessEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .moduleId(model.getModuleId())
                .organizationId(model.getOrganizationId())
                .isActive(model.getIsActive())
                .grantedBy(model.getGrantedBy())
                .grantedAt(localDateTimeToLong(model.getGrantedAt()))
                .expiresAt(localDateTimeToLong(model.getExpiresAt()))
                .description(model.getDescription())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public UserModuleAccessModel toModel(UserModuleAccessEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserModuleAccessModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .moduleId(entity.getModuleId())
                .organizationId(entity.getOrganizationId())
                .isActive(entity.getIsActive())
                .grantedBy(entity.getGrantedBy())
                .grantedAt(longToLocalDateTime(entity.getGrantedAt()))
                .expiresAt(longToLocalDateTime(entity.getExpiresAt()))
                .description(entity.getDescription())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<UserModuleAccessEntity> toEntityList(List<UserModuleAccessModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<UserModuleAccessModel> toModelList(List<UserModuleAccessEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public UserModuleAccessEntity buildNewAccess(Long userId, Long moduleId, Long organizationId,
            Long grantedBy, String moduleDescription) {
        return UserModuleAccessEntity.builder()
                .userId(userId)
                .moduleId(moduleId)
                .organizationId(organizationId)
                .isActive(true)
                .grantedBy(grantedBy)
                .grantedAt(System.currentTimeMillis())
                .expiresAt(null) // No expiration by default
                .description(moduleDescription)
                .build();
    }

    public UserModuleAccessEntity buildNewAccessWithExpiration(Long userId, Long moduleId, Long organizationId,
            Long grantedBy, Long expiresAt, String moduleDescription) {
        return UserModuleAccessEntity.builder()
                .userId(userId)
                .moduleId(moduleId)
                .organizationId(organizationId)
                .isActive(true)
                .grantedBy(grantedBy)
                .grantedAt(System.currentTimeMillis())
                .expiresAt(expiresAt)
                .description(moduleDescription)
                .build();
    }

    public UserModuleAccessResponse toModuleAccessResponse(UserModuleAccessEntity userAccess, ModuleEntity module) {
        if (userAccess == null || module == null) {
            return null;
        }

        return UserModuleAccessResponse.builder()
                .userId(userAccess.getUserId())
                .organizationId(userAccess.getOrganizationId())
                .isActive(userAccess.getIsActive())
                .moduleId(userAccess.getModuleId())
                .moduleName(module.getModuleName())
                .moduleCode(module.getCode())
                .moduleDescription(module.getDescription())
                .grantedAt(userAccess.getGrantedAt())
                .build();
    }
}
