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
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.UpdateUserRolesRequest;
import serp.project.account.core.domain.dto.request.UpdateUserStatusRequest;
import serp.project.account.core.domain.dto.request.UpdateUserTypeRequest;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/users")
@Slf4j
public class OrganizationUserController {
    private final UserUseCase userUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createUserForOrganization(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateUserForOrgRequest request

    ) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.FORBIDDEN);
        }
        var response = userUseCase.createUserForOrganization(organizationId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatusInOrganization(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        boolean isSerpAdmin = authUtils.isSystemAdmin();
        Long updatedBy = authUtils.getCurrentUserId().orElse(null);
        var response = userUseCase.updateUserStatus(
                organizationId,
                updatedBy,
                userId,
                request.getStatus(),
                isSerpAdmin);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = userUseCase.getUserStats(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{userId}/detail")
    public ResponseEntity<?> getUserDetail(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = userUseCase.getUserDetail(organizationId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<?> updateUserRoles(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = userUseCase.updateUserRoles(organizationId, userId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{userId}/type")
    public ResponseEntity<?> updateUserType(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserTypeRequest request) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = userUseCase.updateUserType(organizationId, userId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<?> resetUserPassword(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        var response = userUseCase.resetUserPassword(organizationId, userId, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportUsers(
            @PathVariable Long organizationId,
            @RequestParam(required = false, defaultValue = "csv") String format) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = userUseCase.exportUsers(organizationId, format);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
