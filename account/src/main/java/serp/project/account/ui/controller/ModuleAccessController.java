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
@RequestMapping("/api/v1/module-access")
@Slf4j
public class ModuleAccessController {

    private final ModuleAccessUseCase moduleAccessUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @GetMapping("/organizations/{organizationId}/modules/{moduleId}/check")
    public ResponseEntity<?> canOrganizationAccessModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/module-access/organizations/{}/modules/{}/check - Checking access",
                organizationId, moduleId);
        var response = moduleAccessUseCase.canOrganizationAccessModule(organizationId, moduleId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/{organizationId}/modules")
    public ResponseEntity<?> getAccessibleModulesForOrganization(@PathVariable Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/module-access/organizations/{}/modules - Fetching accessible modules", organizationId);
        var response = moduleAccessUseCase.getAccessibleModulesForOrganization(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/organizations/{organizationId}/assign")
    public ResponseEntity<?> assignUserToModule(
            @PathVariable Long organizationId,
            @Valid @RequestBody AssignUserToModuleRequest request) {
        Long assignedBy = authUtils.getCurrentUserId().orElse(null);
        if (assignedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("POST /api/v1/module-access/organizations/{}/assign - Assigning user {} to module {}",
                organizationId, request.getUserId(), request.getModuleId());
        var response = moduleAccessUseCase.assignUserToModule(organizationId, request, assignedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/bulk-assign")
    public ResponseEntity<?> bulkAssignUsersToModule(@Valid @RequestBody BulkAssignUsersRequest request) {
        Long assignedBy = authUtils.getCurrentUserId().orElse(null);
        if (assignedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(request.getOrganizationId())) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("POST /api/v1/module-access/bulk-assign - Bulk assigning {} users to module {} in org {}",
                request.getUserIds().size(), request.getModuleId(), request.getOrganizationId());
        var response = moduleAccessUseCase.bulkAssignUsersToModule(request, assignedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/organizations/{organizationId}/modules/{moduleId}/users/{userId}")
    public ResponseEntity<?> revokeUserAccessToModule(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @PathVariable Long moduleId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("DELETE /api/v1/module-access/organizations/{}/modules/{}/users/{} - Revoking access",
                organizationId, moduleId, userId);
        var response = moduleAccessUseCase.revokeUserAccessToModule(userId, moduleId, organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/{organizationId}/modules/{moduleId}/users")
    public ResponseEntity<?> getUsersWithAccessToModule(
            @PathVariable Long organizationId,
            @PathVariable Long moduleId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/module-access/organizations/{}/modules/{}/users - Fetching users with access",
                organizationId, moduleId);
        var response = moduleAccessUseCase.getUsersWithAccessToModule(moduleId, organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/users/me/organizations/{organizationId}/modules")
    public ResponseEntity<?> getModulesAccessibleByUser(
            @PathVariable Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (userId == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/module-access/users/me/organizations/{}/modules - Fetching user's accessible modules",
                organizationId);
        var response = moduleAccessUseCase.getModulesAccessibleByUser(userId, organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
