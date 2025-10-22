/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.usecase.OrganizationUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class OrganizationController {
    private final OrganizationUseCase organizationUseCase;

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

}
