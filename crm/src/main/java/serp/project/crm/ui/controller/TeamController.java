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
import serp.project.crm.core.domain.dto.request.CreateTeamRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamRequest;
import serp.project.crm.core.usecase.TeamUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

/**
 * Team Controller - REST API endpoints for team management
 */
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamUseCase teamUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("POST /api/v1/teams - Creating team for tenant: {}", tenantId);
        var response = teamUseCase.createTeam(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PUT /api/v1/teams/{} - Updating team for tenant: {}", id, tenantId);
        var response = teamUseCase.updateTeam(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/teams/{} - Fetching team for tenant: {}", id, tenantId);
        var response = teamUseCase.getTeamById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllTeams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/teams - Fetching all teams for tenant: {}, page: {}, size: {}", tenantId, page, size);
        
        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();
        
        var response = teamUseCase.getAllTeams(tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("DELETE /api/v1/teams/{} - Deleting team for tenant: {}", id, tenantId);
        var response = teamUseCase.deleteTeam(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
