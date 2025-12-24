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
import serp.project.crm.core.domain.dto.request.CreateTeamRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamMemberRequest;
import serp.project.crm.core.domain.dto.request.UpdateTeamRequest;
import serp.project.crm.core.usecase.TeamMemberUseCase;
import serp.project.crm.core.usecase.TeamUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamUseCase teamUseCase;
    private final TeamMemberUseCase teamMemberUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
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
            return null;
        }

        log.info("PUT /api/v1/teams/{} - Updating team for tenant: {}", id, tenantId);
        var response = teamUseCase.updateTeam(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
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
            return null;
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

        log.info("PUT /api/v1/teams/{}/members/{} - Updating team member for tenant: {}", id, memberId, tenantId);
        var response = teamMemberUseCase.updateTeamMember(memberId, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<?> removeTeamMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);

        log.info("DELETE /api/v1/teams/{}/members/{} - Removing team member for tenant: {}", id, memberId, tenantId);
        var response = teamMemberUseCase.removeTeamMember(memberId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllTeams(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        log.info("GET /api/v1/teams - Fetching all teams for tenant: {}, page: {}, size: {}", tenantId, page,
                size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();

        var response = teamUseCase.getAllTeams(tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        log.info("DELETE /api/v1/teams/{} - Deleting team for tenant: {}", id, tenantId);
        var response = teamUseCase.deleteTeam(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
