/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.CreateClientRoleDto;
import serp.project.account.core.domain.dto.request.CreateRealmRoleDto;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.dto.request.UpdateRoleDto;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.enums.RoleEnum;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;
import serp.project.account.infrastructure.store.model.RoleModel;
import serp.project.account.kernel.utils.ConvertUtils;

@Component
@RequiredArgsConstructor
public class RoleMapper extends BaseMapper {

    private final ConvertUtils convertUtils;

    public RoleEntity toEntity(RoleModel model) {
        if (model == null) {
            return null;
        }

        return RoleEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .isRealmRole(model.getIsRealmRole())
                .keycloakClientId(model.getKeycloakClientId())
                .priority(model.getPriority())
                .scope(model.getScope())
                .scopeId(model.getScopeId())
                .moduleId(model.getModuleId())
                .organizationId(model.getOrganizationId())
                .departmentId(model.getDepartmentId())
                .parentRoleId(model.getParentRoleId())
                .roleType(model.getRoleType())
                .isDefault(model.getIsDefault())
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
                .isRealmRole(entity.getIsRealmRole())
                .keycloakClientId(entity.getKeycloakClientId())
                .priority(entity.getPriority())
                .scope(entity.getScope())
                .scopeId(entity.getScopeId())
                .moduleId(entity.getModuleId())
                .organizationId(entity.getOrganizationId())
                .departmentId(entity.getDepartmentId())
                .parentRoleId(entity.getParentRoleId())
                .roleType(entity.getRoleType())
                .isDefault(entity.getIsDefault())
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

    public RoleEntity createRoleMapper(CreateRoleDto request) {
        if (request == null) {
            return null;
        }

        return RoleEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isRealmRole(request.getIsRealmRole() != null ? request.getIsRealmRole() : false)
                .keycloakClientId(request.getKeycloakClientId())
                .priority(request.getPriority())
                .scope(convertUtils.convertStringToEnum(request.getScope(), RoleScope.class))
                .scopeId(request.getScopeId())
                .moduleId(request.getModuleId())
                .organizationId(request.getOrganizationId())
                .departmentId(request.getDepartmentId())
                .parentRoleId(request.getParentRoleId())
                .roleType(convertUtils.convertStringToEnum(request.getRoleType(), RoleType.class))
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();
    }

    public CreateRealmRoleDto toCreateRealmRoleDto(CreateRoleDto roleDto) {
        if (roleDto == null) {
            return null;
        }
        return CreateRealmRoleDto.builder()
                .name(roleDto.getName())
                .description(roleDto.getDescription())
                .build();
    }

    public CreateClientRoleDto toCreateClientRoleDto(CreateRoleDto roleDto) {
        if (roleDto == null) {
            return null;
        }
        return CreateClientRoleDto.builder()
                .name(roleDto.getName())
                .description(roleDto.getDescription())
                .clientId(roleDto.getKeycloakClientId())
                .build();
    }

    public List<CreateRoleDto> fromRoleEnumListToCreateDto(List<RoleEnum> roleEnums) {
        return fromRoleEnumListToCreateDto(roleEnums, null);
    }

    public List<CreateRoleDto> fromRoleEnumListToCreateDto(List<RoleEnum> roleEnums, String keycloakClientId) {
        if (roleEnums == null) {
            return null;
        }

        return roleEnums.stream()
                .map(roleEnum -> CreateRoleDto.builder()
                        .name(roleEnum.getRoleName())
                        .description(roleEnum.getDescription())
                        .isRealmRole(roleEnum.getIsRealmRole())
                        .keycloakClientId(keycloakClientId)
                        .priority(roleEnum.getPriority())
                        .scope(roleEnum.getScope().toString())
                        .roleType(roleEnum.getType().toString())
                        .isDefault(roleEnum.getIsDefault())
                        .build())
                .toList();
    }

    public RoleEntity fromRoleEnum(RoleEnum roleEnum) {
        if (roleEnum == null) {
            return null;
        }

        return RoleEntity.builder()
                .name(roleEnum.getRoleName())
                .description(roleEnum.getDescription())
                .isRealmRole(roleEnum.getIsRealmRole())
                .priority(roleEnum.getPriority())
                .scope(roleEnum.getScope())
                .roleType(roleEnum.getType())
                .isDefault(false)
                .build();
    }

    public List<RoleEntity> fromRoleEnumList(List<RoleEnum> roleEnums) {
        if (roleEnums == null) {
            return null;
        }

        return roleEnums.stream()
                .map(this::fromRoleEnum)
                .collect(Collectors.toList());
    }

    /**
     * Create role with organization context
     */
    public RoleEntity createOrganizationRole(String name, String description, RoleType roleType,
            Long organizationId, Integer priority) {
        return RoleEntity.builder()
                .name(name)
                .description(description)
                .scope(RoleScope.ORGANIZATION)
                .roleType(roleType)
                .organizationId(organizationId)
                .scopeId(organizationId)
                .priority(priority != null ? priority : 5)
                .isRealmRole(true)
                .isDefault(false)
                .build();
    }

    /**
     * Create role with module context
     */
    public RoleEntity createModuleRole(String name, String description, RoleType roleType,
            Long moduleId, Long organizationId, Integer priority) {
        return RoleEntity.builder()
                .name(name)
                .description(description)
                .scope(RoleScope.MODULE)
                .roleType(roleType)
                .moduleId(moduleId)
                .organizationId(organizationId)
                .scopeId(moduleId)
                .priority(priority != null ? priority : 6)
                .isRealmRole(false)
                .isDefault(false)
                .build();
    }

    /**
     * Create role with department context
     */
    public RoleEntity createDepartmentRole(String name, String description, RoleType roleType,
            Long departmentId, Long organizationId, Integer priority) {
        return RoleEntity.builder()
                .name(name)
                .description(description)
                .scope(RoleScope.DEPARTMENT)
                .roleType(roleType)
                .departmentId(departmentId)
                .organizationId(organizationId)
                .scopeId(departmentId)
                .priority(priority != null ? priority : 7)
                .isRealmRole(false)
                .isDefault(false)
                .build();
    }


    /**
     * Update RoleEntity with data from UpdateRoleDto
     */
    public RoleEntity updateRoleFromUpdateDto(RoleEntity existingRole, UpdateRoleDto updateDto) {
        if (existingRole == null || updateDto == null) {
            return existingRole;
        }

        if (updateDto.getName() != null && !updateDto.getName().trim().isEmpty()) {
            existingRole.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null) {
            existingRole.setDescription(updateDto.getDescription());
        }
        if (updateDto.getIsRealmRole() != null) {
            existingRole.setIsRealmRole(updateDto.getIsRealmRole());
        }
        if (updateDto.getKeycloakClientId() != null) {
            existingRole.setKeycloakClientId(updateDto.getKeycloakClientId());
        }
        if (updateDto.getPriority() != null) {
            existingRole.setPriority(updateDto.getPriority());
        }
        if (updateDto.getScope() != null) {
            existingRole.setScope(convertUtils.convertStringToEnum(updateDto.getScope(), RoleScope.class));
        }
        if (updateDto.getScopeId() != null) {
            existingRole.setScopeId(updateDto.getScopeId());
        }
        if (updateDto.getModuleId() != null) {
            existingRole.setModuleId(updateDto.getModuleId());
        }
        if (updateDto.getOrganizationId() != null) {
            existingRole.setOrganizationId(updateDto.getOrganizationId());
        }
        if (updateDto.getDepartmentId() != null) {
            existingRole.setDepartmentId(updateDto.getDepartmentId());
        }
        if (updateDto.getParentRoleId() != null) {
            existingRole.setParentRoleId(updateDto.getParentRoleId());
        }
        if (updateDto.getRoleType() != null) {
            existingRole.setRoleType(convertUtils.convertStringToEnum(updateDto.getRoleType(), RoleType.class));
        }
        if (updateDto.getIsDefault() != null) {
            existingRole.setIsDefault(updateDto.getIsDefault());
        }

        return existingRole;
    }

    /**
     * Validate role data consistency between entity and model
     */
    public boolean isValidMapping(RoleEntity entity, RoleModel model) {
        if (entity == null || model == null) {
            return false;
        }

        return entity.getName().equals(model.getName()) &&
                entity.getScope().equals(model.getScope()) &&
                entity.getRoleType().equals(model.getRoleType()) &&
                entity.getPriority().equals(model.getPriority());
    }
}
