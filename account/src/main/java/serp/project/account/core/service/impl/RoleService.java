/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.GroupRoleEntity;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.RolePermissionEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IGroupRolePort;
import serp.project.account.core.port.store.IPermissionPort;
import serp.project.account.core.port.store.IRolePermissionPort;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.core.service.IRoleService;
import serp.project.account.infrastructure.store.mapper.RoleMapper;
import serp.project.account.kernel.utils.CollectionUtils;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final IRolePort rolePort;
    private final IGroupRolePort groupRolePort;
    private final IRolePermissionPort rolePermissionPort;
    private final IPermissionPort permissionPort;

    private final RoleMapper roleMapper;

    @Override
    public RoleEntity createRole(CreateRoleDto request) {
        var existedRole = rolePort.getRoleByName(request.getName());
        if (existedRole != null) {
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
                            .build())
                    .collect(Collectors.toList());
            rolePermissionPort.saveAll(rolePermissions);
        }

        return role;
    }

    @Override
    public RoleEntity getRoleByName(String name) {
        return rolePort.getRoleByName(name);
    }

    @Override
    public List<RoleEntity> getAllRoles() {
        // Need cache later
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
        return roles;
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
                        .build())
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(newRolePermissions)) {
            rolePermissionPort.saveAll(newRolePermissions);
        }
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

}
