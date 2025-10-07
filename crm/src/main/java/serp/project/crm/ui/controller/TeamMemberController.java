/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.CreateTeamMemberRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamMemberRequest;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.usecase.TeamMemberUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

/**
 * Team Member Controller - REST API endpoints for team member management
 */
@RestController
@RequestMapping("/api/v1/team-members")
@RequiredArgsConstructor
@Slf4j
public class TeamMemberController {

    private final TeamMemberUseCase teamMemberUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> addTeamMember(@Valid @RequestBody CreateTeamMemberRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("POST /api/v1/team-members - Adding team member for tenant: {}", tenantId);
        var response = teamMemberUseCase.addTeamMember(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeamMember(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamMemberRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PUT /api/v1/team-members/{} - Updating team member for tenant: {}", id, tenantId);
        var response = teamMemberUseCase.updateTeamMember(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamMemberById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/team-members/{} - Fetching team member for tenant: {}", id, tenantId);
        var response = teamMemberUseCase.getTeamMemberById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<?> getTeamMembersByTeam(
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/team-members/team/{} - Fetching team members for tenant: {}", teamId, tenantId);
        
        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();
        
        var response = teamMemberUseCase.getTeamMembersByTeam(teamId, tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<?> changeRole(
            @PathVariable Long id,
            @RequestParam String newRole) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PATCH /api/v1/team-members/{}/role - Changing role to {} for tenant: {}", id, newRole, tenantId);
        var response = teamMemberUseCase.changeRole(id, newRole, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(
            @PathVariable Long id,
            @RequestParam TeamMemberStatus newStatus) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PATCH /api/v1/team-members/{}/status - Changing status to {} for tenant: {}", id, newStatus, tenantId);
        var response = teamMemberUseCase.changeStatus(id, newStatus, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeTeamMember(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("DELETE /api/v1/team-members/{} - Removing team member for tenant: {}", id, tenantId);
        var response = teamMemberUseCase.removeTeamMember(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
