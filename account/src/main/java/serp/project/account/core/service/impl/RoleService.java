/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.RolePermissionEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IPermissionPort;
import serp.project.account.core.port.store.IRolePermissionPort;
import serp.project.account.core.port.store.IRolePort;
import serp.project.account.core.service.IRoleService;
import serp.project.account.infrastructure.store.mapper.RoleMapper;
import serp.project.account.kernel.property.KeycloakProperties;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final IRolePort rolePort;
    private final IRolePermissionPort rolePermissionPort;
    private final IPermissionPort permissionPort;

    private final Keycloak keycloakAdmin;
    private final KeycloakProperties keycloakProperties;

    private final RoleMapper roleMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
                                .toList()
                );
            }
        });
        return roles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleEntity createRealmRole(CreateRoleDto request) {
        var existedRole = rolePort.getRoleByName(request.getName());
        if (existedRole != null) {
            throw new AppException(Constants.ErrorMessage.ROLE_ALREADY_EXISTS);
        }
        var role = roleMapper.createRoleMapper(request);
        role = rolePort.save(role);

        createRealmRole(role.getId(), request.getName(), request.getDescription());

        return role;
    }

    private void createRealmRole(Long roleId, String roleName, String description) {
        RealmResource realmResource = keycloakAdmin.realm(keycloakProperties.getRealm());
        RolesResource roleResource = realmResource.roles();

        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription(description);
        role.setComposite(false);

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("role_id", List.of(String.valueOf(roleId)));
        attributes.put("created_by", List.of(keycloakProperties.getClientId()));
        attributes.put("created_at", List.of(String.valueOf(System.currentTimeMillis())));
        role.setAttributes(attributes);

        roleResource.create(role);
    }

}
