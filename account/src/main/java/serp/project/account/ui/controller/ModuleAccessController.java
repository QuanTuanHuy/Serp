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
import serp.project.account.core.usecase.ModuleAccessUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}")
@Slf4j
public class ModuleAccessController {

    private final ModuleAccessUseCase moduleAccessUseCase;
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
            @PathVariable Long moduleId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/organizations/{}/modules/{}/users - Fetching users with access",
                organizationId, moduleId);
        var response = moduleAccessUseCase.getUsersWithAccessToModule(organizationId, moduleId);
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
}
