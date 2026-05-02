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
import serp.project.crm.core.domain.dto.request.AssignOpportunityRequest;
import serp.project.crm.core.domain.dto.request.ChangeOpportunityStageRequest;
import serp.project.crm.core.domain.dto.request.CreateOpportunityRequest;
import serp.project.crm.core.domain.dto.request.OpportunityFilterRequest;
import serp.project.crm.core.domain.dto.request.PipelineFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateOpportunityRequest;
import serp.project.crm.core.usecase.ActivityUseCase;
import serp.project.crm.core.usecase.OpportunityUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/opportunities")
@RequiredArgsConstructor
@Slf4j
public class OpportunityController {

    private final OpportunityUseCase opportunityUseCase;
    private final ActivityUseCase activityUseCase;

    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createOpportunity(@Valid @RequestBody CreateOpportunityRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        log.info("POST /api/v1/opportunities - Creating opportunity for tenant: {}", tenantId);
        var response = opportunityUseCase.createOpportunity(request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOpportunityRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        log.info("PUT /api/v1/opportunities/{} - Updating opportunity for tenant: {}", id, tenantId);
        var response = opportunityUseCase.updateOpportunity(id, request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOpportunityById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        log.info("GET /api/v1/opportunities/{} - Fetching opportunity for tenant: {}", id, tenantId);
        var response = opportunityUseCase.getOpportunityById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<?> getActivitiesByOpportunityId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        log.info("GET /api/v1/opportunities/{}/activities - Fetching activities for tenant: {}, page: {}, size: {}",
                id, tenantId, page, size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();
        var response = activityUseCase.getActivitiesByOpportunity(id, tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllOpportunities(
            @ModelAttribute OpportunityFilterRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        OpportunityFilterRequest safeRequest = request != null ? request : OpportunityFilterRequest.builder().build();
        log.info("GET /api/v1/opportunities - Filtering opportunities for tenant: {}", tenantId);
        var response = opportunityUseCase.filterOpportunities(safeRequest, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/pipeline")
    public ResponseEntity<?> getPipeline(@ModelAttribute PipelineFilterRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        var response = opportunityUseCase.getPipeline(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<?> filterOpportunities(@RequestBody(required = false) OpportunityFilterRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        OpportunityFilterRequest safeRequest = request != null ? request : OpportunityFilterRequest.builder().build();

        log.info("POST /api/v1/opportunities/search - Filtering opportunities for tenant: {}", tenantId);
        var response = opportunityUseCase.filterOpportunities(safeRequest, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/stage")
    public ResponseEntity<?> changeStage(
            @PathVariable Long id,
            @Valid @RequestBody ChangeOpportunityStageRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        log.info("PUT /api/v1/opportunities/{}/stage - Changing stage to {} for tenant: {}", id, request.getStage(),
                tenantId);
        var response = opportunityUseCase.changeOpportunityStage(id, request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody AssignOpportunityRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        var response = opportunityUseCase.assignOpportunity(id, request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOpportunity(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        log.info("DELETE /api/v1/opportunities/{} - Deleting opportunity for tenant: {}", id, tenantId);
        var response = opportunityUseCase.deleteOpportunity(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
