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
import serp.project.crm.core.domain.dto.request.CreateOpportunityRequest;
import serp.project.crm.core.domain.dto.request.UpdateOpportunityRequest;
import serp.project.crm.core.domain.enums.OpportunityStage;
import serp.project.crm.core.usecase.OpportunityUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

/**
 * Opportunity Controller - REST API endpoints for opportunity management
 */
@RestController
@RequestMapping("/api/v1/opportunities")
@RequiredArgsConstructor
@Slf4j
public class OpportunityController {

    private final OpportunityUseCase opportunityUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createOpportunity(@Valid @RequestBody CreateOpportunityRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("POST /api/v1/opportunities - Creating opportunity for tenant: {}", tenantId);
        var response = opportunityUseCase.createOpportunity(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOpportunity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOpportunityRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PUT /api/v1/opportunities/{} - Updating opportunity for tenant: {}", id, tenantId);
        var response = opportunityUseCase.updateOpportunity(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOpportunityById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/opportunities/{} - Fetching opportunity for tenant: {}", id, tenantId);
        var response = opportunityUseCase.getOpportunityById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllOpportunities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/opportunities - Fetching all opportunities for tenant: {}, page: {}, size: {}", tenantId, page, size);
        
        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();
        
        var response = opportunityUseCase.getAllOpportunities(tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<?> changeStage(
            @PathVariable Long id,
            @RequestParam OpportunityStage newStage) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PATCH /api/v1/opportunities/{}/stage - Changing stage to {} for tenant: {}", id, newStage, tenantId);
        var response = opportunityUseCase.changeOpportunityStage(id, newStage, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{id}/close-won")
    public ResponseEntity<?> closeAsWon(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("POST /api/v1/opportunities/{}/close-won - Closing as won for tenant: {}", id, tenantId);
        var response = opportunityUseCase.closeOpportunityAsWon(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{id}/close-lost")
    public ResponseEntity<?> closeAsLost(
            @PathVariable Long id,
            @RequestParam String lostReason) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("POST /api/v1/opportunities/{}/close-lost - Closing as lost for tenant: {}", id, tenantId);
        var response = opportunityUseCase.closeOpportunityAsLost(id, lostReason, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOpportunity(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("DELETE /api/v1/opportunities/{} - Deleting opportunity for tenant: {}", id, tenantId);
        var response = opportunityUseCase.deleteOpportunity(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
