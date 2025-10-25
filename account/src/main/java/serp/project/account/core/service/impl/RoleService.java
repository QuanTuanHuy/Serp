/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.CacheConstants;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.dto.request.UpdateRoleDto;
import serp.project.account.core.domain.entity.GroupRoleEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.RolePermissionEntity;
import serp.project.account.core.domain.enums.RoleScope;
import serp.project.account.core.domain.enums.RoleType;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.ICachePort;
import serp.project.account.core.port.store.IGroupRolePort;
import serp.project.account.core.port.store.IPermissionPort;
import serp.project.account.core.port.store.IRolePermissionPort;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.core.service.IRoleService;
import serp.project.account.infrastructure.store.mapper.RoleMapper;
import serp.project.account.kernel.utils.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService implements IRoleService {
    private final IRolePort rolePort;
    private final IGroupRolePort groupRolePort;
    private final IRolePermissionPort rolePermissionPort;
    private final IPermissionPort permissionPort;

    private final ICachePort cachePort;

    private final RoleMapper roleMapper;

    private final AsyncTaskExecutor asyncTaskExecutor;

    @Override
    public RoleEntity createRole(CreateRoleDto request) {
        var existedRole = rolePort.getRoleByName(request.getName());
        if (existedRole != null &&
                existedRole.getKeycloakClientId().equals(request.getKeycloakClientId())) {
            throw new AppException(Constants.ErrorMessage.ROLE_ALREADY_EXISTS);
        }

        var role = roleMapper.createRoleMapper(request);
        role = rolePort.save(role);
        final long roleId = role.getId();

        if (!CollectionUtils.isEmpty(request.getPermissionIds())) {
            List<RolePermissionEntity> rolePermissions = request.getPermissionIds().stream()
                    .map(permissionId -> RolePermissionEntity.builder()
                            .roleId(roleId)
                            .permissionId(permissionId)
                            .createdAt(Instant.now().toEpochMilli())
                            .updatedAt(Instant.now().toEpochMilli())
                            .build())
                    .collect(Collectors.toList());
            rolePermissionPort.saveAll(rolePermissions);
        }

        clearCacheAllRoles();

        return role;
    }

    @Override
    public RoleEntity getRoleByName(String name) {
        return rolePort.getRoleByName(name);
    }

    @Override
    public List<RoleEntity> getAllRoles() {
        List<RoleEntity> cachedRoles = cachePort.getFromCache(CacheConstants.ALL_ROLES,
                new ParameterizedTypeReference<>() {
                });
        if (!CollectionUtils.isEmpty(cachedRoles)) {
            return cachedRoles;
        }

        var roles = rolePort.getAllRoles();
        var permissions = permissionPort.getAllPermissions();
        roles.forEach(role -> {
            var rolePermissions = rolePermissionPort.getRolePermissionsByRoleId(role.getId());
            if (!CollectionUtils.isEmpty(rolePermissions)) {
                var permissionIds = rolePermissions.stream()
                        .map(RolePermissionEntity::getPermissionId)
                        .toList();
                role.setPermissions(
                        permissions.stream()
                                .filter(permission -> permissionIds.contains(permission.getId()))
                                .toList());
            }
        });

        cacheAllRoles(roles);

        return roles;
    }

    @Override
    public List<RoleEntity> getRolesByModuleId(Long moduleId) {
        List<RoleEntity> roles = getAllRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(role -> role.getModuleId() != null && role.getModuleId().equals(moduleId))
                .toList();
    }

    @Override
    public void addPermissionsToRole(Long roleId, List<Long> permissionIds) {
        RoleEntity role = rolePort.getRoleById(roleId);
        if (role == null) {
            throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
        }
        List<RolePermissionEntity> rolePermissions = rolePermissionPort.getRolePermissionsByRoleId(role.getId());
        Set<Long> existingPermissionIds = rolePermissions.stream()
                .map(RolePermissionEntity::getPermissionId)
                .collect(Collectors.toSet());

        List<RolePermissionEntity> newRolePermissions = permissionIds.stream()
                .filter(permissionId -> !existingPermissionIds.contains(permissionId))
                .map(permissionId -> RolePermissionEntity.builder()
                        .roleId(role.getId())
                        .permissionId(permissionId)
                        .createdAt(Instant.now().toEpochMilli())
                        .updatedAt(Instant.now().toEpochMilli())
                        .build())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(newRolePermissions)) {
            rolePermissionPort.saveAll(newRolePermissions);
        }

        clearCacheAllRoles();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleEntity getOrCreateOrganizationRole(String roleName) {
        RoleEntity role = rolePort.getRoleByName(roleName);
        if (role != null) {
            return role;
        }
        RoleEntity newRole = RoleEntity.builder()
                .name(roleName)
                .isRealmRole(false)
                .build();
        return rolePort.save(newRole);
    }

    @Override
    public List<RoleEntity> getRolesByGroupId(Long groupId) {
        List<Long> roleIds = groupRolePort.getByGroupId(groupId).stream()
                .map(GroupRoleEntity::getRoleId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }
        return rolePort.getRolesByIds(roleIds);
    }

    public void cacheAllRoles(List<RoleEntity> roles) {
        asyncTaskExecutor
                .execute(() -> cachePort.setToCache(CacheConstants.ALL_ROLES, roles, CacheConstants.LONG_EXPIRATION));
    }

    public void clearCacheAllRoles() {
        asyncTaskExecutor.execute(() -> cachePort.deleteFromCache(CacheConstants.ALL_ROLES));
    }

    @Override
    public List<RoleEntity> getRolesByScope(RoleScope scope) {
        List<RoleEntity> roles = getAllRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(role -> role.getScope().equals(scope))
                .toList();
    }

    @Override
    public List<RoleEntity> getRolesByScopeAndTypeList(RoleScope scope, List<RoleType> types) {
        List<RoleEntity> roles = getAllRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(role -> role.getScope().equals(scope) && types.contains(role.getRoleType()))
                .toList();
    }

    @Override
    public RoleEntity getHighestRole(List<RoleEntity> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return null;
        }
        roles.sort((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()));
        return roles.getFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleEntity updateRole(Long roleId, UpdateRoleDto updateDto) {
        RoleEntity existingRole = rolePort.getRoleById(roleId);
        if (existingRole == null) {
            throw new AppException(Constants.ErrorMessage.ROLE_NOT_FOUND);
        }

        existingRole = roleMapper.updateRoleFromUpdateDto(existingRole, updateDto);
        existingRole = rolePort.save(existingRole);

        clearCacheAllRoles();

        return existingRole;
    }

}
