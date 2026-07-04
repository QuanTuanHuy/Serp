/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.AssignUserToModuleRequest;
import serp.project.account.core.domain.dto.request.BulkAssignUsersRequest;
import serp.project.account.core.domain.dto.request.BulkModuleAccessUsersRequest;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.dto.request.UpdateModuleAccessSettingsRequest;
import serp.project.account.core.usecase.ModuleAccessUseCase;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
@Slf4j
public class ModuleAccessController {

    private final ModuleAccessUseCase moduleAccessUseCase;
    private final UserUseCase userUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @GetMapping("/modules/{moduleId}/access")
    public ResponseEntity<?> canOrganizationAccessModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/organizations/{}/modules/{}/access - Checking access",
                organizationId, moduleId);
        var response = moduleAccessUseCase.canOrganizationAccessModule(organizationId, moduleId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/modules")
    public ResponseEntity<?> getAccessibleModulesForOrganization(@PathVariable Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/organizations/{}/modules - Fetching accessible modules", organizationId);
        var response = moduleAccessUseCase.getAccessibleModulesForOrganization(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/modules/{moduleId}/users")
    public ResponseEntity<?> assignUserToModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId,
            @Valid @RequestBody AssignUserToModuleRequest request) {
        Long assignedBy = authUtils.getCurrentUserId().orElse(null);
        if (assignedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        request.setModuleId(moduleId);

        log.info("POST /api/v1/organizations/{}/modules/{}/users - Assigning user {} to module",
                organizationId, moduleId, request.getUserId());
        var response = moduleAccessUseCase.assignUserToModule(organizationId, request, assignedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/modules/{moduleId}/users/bulk")
    public ResponseEntity<?> bulkAssignUsersToModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId,
            @Valid @RequestBody BulkAssignUsersRequest request) {
        Long assignedBy = authUtils.getCurrentUserId().orElse(null);
        if (assignedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        request.setOrganizationId(organizationId);
        request.setModuleId(moduleId);

        log.info("POST /api/v1/organizations/{}/modules/{}/users/bulk - Bulk assigning {} users",
                organizationId, moduleId, request.getUserIds().size());
        var response = moduleAccessUseCase.bulkAssignUsersToModule(request, assignedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/modules/{moduleId}/users/bulk-revoke")
    public ResponseEntity<?> bulkRevokeUsersFromModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId,
            @Valid @RequestBody BulkModuleAccessUsersRequest request) {
        Long revokedBy = authUtils.getCurrentUserId().orElse(null);
        if (revokedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("POST /api/v1/organizations/{}/modules/{}/users/bulk-revoke - Bulk revoking {} users",
                organizationId, moduleId, request.getUserIds().size());
        var response = moduleAccessUseCase.bulkRevokeUsersFromModule(organizationId, moduleId, request, revokedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/modules/{moduleId}/users/{userId}")
    public ResponseEntity<?> revokeUserAccessToModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId,
            @PathVariable Long userId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("DELETE /api/v1/organizations/{}/modules/{}/users/{} - Revoking access",
                organizationId, moduleId, userId);
        var response = moduleAccessUseCase.revokeUserAccessToModule(organizationId, userId, moduleId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/modules/{moduleId}/users")
    public ResponseEntity<?> getUsersWithAccessToModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long departmentId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        GetUserParams params = GetUserParams.builder()
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .search(search)
                .status(status)
                .userType(userType)
                .roleId(roleId)
                .departmentId(departmentId)
                .organizationId(organizationId)
                .moduleId(moduleId)
                .build();

        log.info("GET /api/v1/organizations/{}/modules/{}/users - Fetching users with active module access",
                organizationId, moduleId);
        var response = userUseCase.getUsers(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/users/me/modules")
    public ResponseEntity<?> getModulesAccessibleByCurrentUser(@PathVariable Long organizationId) {

        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (userId == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/organizations/{}/users/me/modules - Fetching user's accessible modules",
                organizationId);
        var response = moduleAccessUseCase.getModulesAccessibleByUser(organizationId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/modules/{moduleId}/access-settings")
    public ResponseEntity<?> updateModuleAccessSettings(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId,
            @Valid @RequestBody UpdateModuleAccessSettingsRequest request) {
        Long updatedBy = authUtils.getCurrentUserId().orElse(null);
        if (updatedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        var response = moduleAccessUseCase.updateModuleAccessSettings(
                organizationId, moduleId, request, updatedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/modules/{moduleId}/auto-grant/backfill")
    public ResponseEntity<?> backfillAutoGrant(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId) {
        Long grantedBy = authUtils.getCurrentUserId().orElse(null);
        if (grantedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        var response = moduleAccessUseCase.backfillAutoGrant(organizationId, moduleId, grantedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
