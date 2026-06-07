/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
import serp.project.account.core.domain.dto.request.UpdateOrganizationStatusRequest;
import serp.project.account.core.usecase.OrganizationUseCase;
import serp.project.account.core.usecase.RoleUseCase;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class OrganizationController {
    private final OrganizationUseCase organizationUseCase;
    private final RoleUseCase roleUseCase;
    private final UserUseCase userUseCase;

    private final AuthUtils authUtils;

    @GetMapping("/admin/organizations")
    public ResponseEntity<?> getOrganizations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir) {
        GetOrganizationParams params = GetOrganizationParams.builder()
                .search(search)
                .status(status)
                .type(type)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .build();
        var response = organizationUseCase.getOrganizations(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/admin/organizations/{organizationId}/status")
    public ResponseEntity<?> updateOrganizationStatus(
            @PathVariable Long organizationId,
            @Valid @RequestBody UpdateOrganizationStatusRequest request) {
        Long updatedBy = authUtils.getCurrentUserId().orElse(null);
        var response = organizationUseCase.updateOrganizationStatus(organizationId, updatedBy, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/admin/organizations/{organizationId}/users/stats")
    public ResponseEntity<?> getOrganizationUserStats(@PathVariable Long organizationId) {
        var response = userUseCase.getUserStats(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/admin/organizations/{organizationId}")
    public ResponseEntity<?> getOrganizationById(@PathVariable Long organizationId) {
        var response = organizationUseCase.getOrganizationById(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/{organizationId}/roles")
    public ResponseEntity<?> getRolesForOrganization(@PathVariable Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.NO_PERMISSION_TO_ACCESS_ORGANIZATION);
        }
        var response = roleUseCase.getValidRolesForOrganization(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/me")
    public ResponseEntity<?> getMyOrganization() {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);
        var response = organizationUseCase.getOrganizationById(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/me/settings")
    public ResponseEntity<?> getMyOrganizationSettings() {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);
        var response = organizationUseCase.getOrganizationSettings(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/organizations/me/settings")
    public ResponseEntity<?> updateMyOrganizationSettings(
            @Valid @RequestBody UpdateOrganizationSettingsRequest request) {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);
        var response = organizationUseCase.updateOrganizationSettings(organizationId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
