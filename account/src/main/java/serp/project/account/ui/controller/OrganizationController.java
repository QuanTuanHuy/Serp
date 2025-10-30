/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateUserForOrgRequest;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.UpdateUserStatusRequest;
import serp.project.account.core.usecase.OrganizationUseCase;
import serp.project.account.core.usecase.UserUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class OrganizationController {
    private final OrganizationUseCase organizationUseCase;
    private final UserUseCase userUseCase;

    private final AuthUtils authUtils;

    @GetMapping("/admin/organizations")
    public ResponseEntity<?> getOrganizations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        GetOrganizationParams params = GetOrganizationParams.builder()
                .search(search)
                .status(status)
                .type(type)
                .page(page)
                .pageSize(pageSize)
                .build();
        var response = organizationUseCase.getOrganizations(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/admin/organizations/{organizationId}")
    public ResponseEntity<?> getOrganizationById(@RequestParam Long organizationId) {
        var response = organizationUseCase.getOrganizationById(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/me")
    public ResponseEntity<?> getMyOrganization() {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);
        var response = organizationUseCase.getOrganizationById(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/organizations/{organizationId}/users")
    public ResponseEntity<?> createUserForOrganization(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateUserForOrgRequest request

    ) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            return ResponseEntity.status(403).body(Constants.ErrorMessage.FORBIDDEN);
        }
        var response = organizationUseCase.createUserForOrganization(organizationId, request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/organizations/{organizationId}/users/{userId}/status")
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

}
