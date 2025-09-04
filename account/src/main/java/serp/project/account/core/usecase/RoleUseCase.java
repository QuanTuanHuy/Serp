package serp.project.account.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreateRoleDto;
import serp.project.account.core.domain.entity.PermissionEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.impl.PermissionService;
import serp.project.account.kernel.utils.ResponseUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleUseCase {
    private final IRoleService roleService;
    private final PermissionService permissionService;

    private final ResponseUtils responseUtils;

    public GeneralResponse<?> createRole(CreateRoleDto request) {
        try {
            var permissionIds = request.getPermissionIds();
            List<PermissionEntity> permissions = new ArrayList<>();
            if (!CollectionUtils.isEmpty(permissionIds)) {
                permissions = permissionService.getPermissionsByIds(permissionIds);
                if (permissions.size() != permissionIds.size()) {
                    return responseUtils.badRequest(Constants.ErrorMessage.ONE_OR_MORE_PERMISSIONS_NOT_FOUND);
                }
            }

            var role = roleService.createRole(request);
            role.setPermissions(permissions);
            return responseUtils.success(role);
        } catch (AppException e) {
            log.error("Error creating role: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error when creating role: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> createRealmRole(CreateRoleDto request) {
        try {
            var response = roleService.createRealmRole(request);
            return responseUtils.success(response);
        } catch (Exception e) {
            log.error("Error create realm role: {}", e.getMessage());
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
}
