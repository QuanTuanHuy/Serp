package serp.project.account.core.usecase;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.CreatePermissionDto;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.service.IPermissionService;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionUseCase {
    private final IPermissionService permissionService;
    private final ResponseUtils responseUtils;

    public GeneralResponse<?> createPermission(CreatePermissionDto request) {
        try {
            var permission = permissionService.createPermission(request);
            return responseUtils.success(permission);
        } catch (AppException e) {
            log.error("Error creating permission: {}", e.getMessage());
            return responseUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error creating permission: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getAllPermissions() {
        try {
            var permissions = permissionService.getAllPermissions();
            return responseUtils.success(permissions);
        } catch (Exception e) {
            log.error("Error getting all permissions: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }
}
