package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.PermissionEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IKeycloakRoleService;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.impl.PermissionService;
import serp.project.account.infrastructure.store.mapper.RoleMapper;
import serp.project.account.kernel.utils.DataUtils;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleUseCase {
    private final IRoleService roleService;
    private final IKeycloakRoleService keycloakRoleService;
    private final PermissionService permissionService;

    private final ResponseUtils responseUtils;

    private final RoleMapper roleMapper;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createRole(CreateRoleDto request) {
        try {
            validateCreateRoleRequest(request);

            var permissionIds = request.getPermissionIds();
            List<PermissionEntity> permissions = new ArrayList<>();
            if (!CollectionUtils.isEmpty(permissionIds)) {
                permissions = permissionService.getPermissionsByIds(permissionIds);
                if (permissions.size() != permissionIds.size()) {
                    return responseUtils.badRequest(Constants.ErrorMessage.ONE_OR_MORE_PERMISSIONS_NOT_FOUND);
                }
            }

            if (request.getIsRealmRole()) {
                keycloakRoleService.createRealmRole(roleMapper.toCreateRealmRoleDto(request));
            } else {
                keycloakRoleService.createClientRole(roleMapper.toCreateClientRoleDto(request));
            }

            var role = roleService.createRole(request);
            role.setPermissions(permissions);
            return responseUtils.success(role);
        } catch (AppException e) {
            log.error("Error creating role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error when creating role: {}", e.getMessage());
            throw e;
        }
    }

    public GeneralResponse<?> addPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            var permissions = permissionService.getPermissionsByIds(permissionIds);
            if (permissions.size() != permissionIds.size()) {
                return responseUtils.badRequest(Constants.ErrorMessage.ONE_OR_MORE_PERMISSIONS_NOT_FOUND);
            }
            roleService.addPermissionsToRole(roleId, permissionIds);
            return responseUtils.success("Permissions added to role successfully");
        } catch (AppException e) {
            log.error("Error adding permissions to role: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when adding permissions to role: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getAllRoles() {
        try {
            var roles = roleService.getAllRoles();
            return responseUtils.success(roles);
        } catch (Exception e) {
            log.error("Error getting all roles: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    private void validateCreateRoleRequest(CreateRoleDto request) {
        if (request.getIsRealmRole() == false && DataUtils.isNullOrEmpty(request.getKeycloakClientId())) {
            throw new AppException(Constants.ErrorMessage.CLIENT_NOT_FOUND);
        }
    }
}
