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
import serp.project.crm.core.domain.dto.request.AssignTeamTerritoriesRequest;
import serp.project.crm.core.domain.dto.request.ChangeTeamManagerRequest;
import serp.project.crm.core.domain.dto.request.CreateTeamMemberRequest;
import serp.project.crm.core.domain.dto.request.CreateTeamRequest;
import serp.project.crm.core.domain.dto.request.ReassignInactiveMemberRecordsRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamMemberRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamRequest;
import serp.project.crm.core.usecase.TeamMemberUseCase;
import serp.project.crm.core.usecase.TeamUseCase;
import serp.project.crm.kernel.utils.AuthUtils;
import serp.project.crm.kernel.utils.ResponseUtils;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamUseCase teamUseCase;
    private final TeamMemberUseCase teamMemberUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    private ResponseEntity<?> unauthorizedResponse() {
        var response = responseUtils.unauthorized("Authentication context is required");
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return unauthorizedResponse();
        }

        log.info("[TeamController] POST /api/v1/teams - Creating team for tenant: {}", tenantId);
        var response = teamUseCase.createTeam(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTeamRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("PUT /api/v1/teams/{} - Updating team for tenant: {}", id, tenantId);
        var response = teamUseCase.updateTeam(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("GET /api/v1/teams/{} - Fetching team for tenant: {}", id, tenantId);
        var response = teamUseCase.getTeamById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<?> getTeamMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        var pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();

        log.info("GET /api/v1/teams/{}/members - Fetching team members for tenant: {}", id, tenantId);
        var response = teamMemberUseCase.getTeamMembersByTeam(id, tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addTeamMember(
            @PathVariable Long id,
            @Valid @RequestBody CreateTeamMemberRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        request.setTeamId(id);

        log.info("POST /api/v1/teams/{}/members - Adding team member for tenant: {}", id, tenantId);
        var response = teamMemberUseCase.addTeamMember(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> updateTeamMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateTeamMemberRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("PUT /api/v1/teams/{}/members/{} - Updating team member for tenant: {}", id, memberId, tenantId);
        var response = teamMemberUseCase.updateTeamMember(id, memberId, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> removeTeamMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("DELETE /api/v1/teams/{}/members/{} - Removing team member for tenant: {}", id, memberId, tenantId);
        var response = teamMemberUseCase.removeTeamMember(id, memberId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{id}/members/{memberId}/reassign")
    public ResponseEntity<?> removeAndReassignTeamMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @Valid @RequestBody ReassignInactiveMemberRecordsRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("POST /api/v1/teams/{}/members/{}/reassign - Removing member and reassigning records for tenant: {}",
                id, memberId, tenantId);
        var response = teamMemberUseCase.removeAndReassignTeamMember(id, memberId, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllTeams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("GET /api/v1/teams - Fetching all teams for tenant: {}, page: {}, size: {}", tenantId, page,
                size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();

        var response = teamUseCase.getAllTeams(tenantId, pageRequest, status);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/manager")
    public ResponseEntity<?> changeManager(
            @PathVariable Long id,
            @Valid @RequestBody ChangeTeamManagerRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("PUT /api/v1/teams/{}/manager - Changing team manager for tenant: {}", id, tenantId);
        var response = teamUseCase.changeManager(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/territories")
    public ResponseEntity<?> assignTerritories(
            @PathVariable Long id,
            @Valid @RequestBody AssignTeamTerritoriesRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return unauthorizedResponse();
        }

        log.info("PUT /api/v1/teams/{}/territories - Assigning territories for tenant: {}", id, tenantId);
        var response = teamUseCase.assignTerritories(id, request, tenantId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        log.info("DELETE /api/v1/teams/{} - Deleting team for tenant: {}", id, tenantId);
        var response = teamUseCase.deleteTeam(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
