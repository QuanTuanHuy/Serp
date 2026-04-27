/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.crm.core.domain.dto.request.CreateTerritoryRequest;
import serp.project.crm.core.domain.dto.request.ManualRerouteLeadRequest;
import serp.project.crm.core.domain.dto.request.ResolveTerritoryRequest;
import serp.project.crm.core.domain.dto.request.TerritoryFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateTerritoryRequest;
import serp.project.crm.core.usecase.TerritoryUseCase;
import serp.project.crm.kernel.utils.AuthUtils;
import serp.project.crm.kernel.utils.ResponseUtils;

@RestController
@RequestMapping("/api/v1/territories")
@RequiredArgsConstructor
public class TerritoryController {

    private final TerritoryUseCase territoryUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    private ResponseEntity<?> unauthorizedResponse() {
        var response = responseUtils.unauthorized("Authentication context is required");
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping
    public ResponseEntity<?> createTerritory(@Valid @RequestBody CreateTerritoryRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return unauthorizedResponse();
        }

        var response = territoryUseCase.createTerritory(request, tenantId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{territoryCode}")
    public ResponseEntity<?> updateTerritory(@PathVariable String territoryCode,
            @Valid @RequestBody UpdateTerritoryRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return unauthorizedResponse();
        }

        var response = territoryUseCase.updateTerritory(territoryCode, request, tenantId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{territoryCode}/activate")
    public ResponseEntity<?> activateTerritory(@PathVariable String territoryCode) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return unauthorizedResponse();
        }

        var response = territoryUseCase.activateTerritory(territoryCode, tenantId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{territoryCode}/deactivate")
    public ResponseEntity<?> deactivateTerritory(@PathVariable String territoryCode) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return unauthorizedResponse();
        }

        var response = territoryUseCase.deactivateTerritory(territoryCode, tenantId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{territoryCode}")
    public ResponseEntity<?> getTerritoryByCode(@PathVariable String territoryCode) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        var response = territoryUseCase.getTerritoryByCode(territoryCode, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getTerritories(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String parentTerritoryCode,
            @RequestParam(required = false) String source) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        var response = territoryUseCase.getTerritories(TerritoryFilterRequest.builder()
                .keyword(keyword)
                .active(active)
                .parentTerritoryCode(parentTerritoryCode)
                .source(source)
                .build(), tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{territoryCode}/owner")
    public ResponseEntity<?> getTerritoryOwner(@PathVariable String territoryCode) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        var response = territoryUseCase.getTerritoryOwner(territoryCode, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/resolve")
    public ResponseEntity<?> resolveTerritory(@RequestBody(required = false) ResolveTerritoryRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return unauthorizedResponse();
        }

        var safeRequest = request != null ? request : ResolveTerritoryRequest.builder().build();
        var response = territoryUseCase.resolveTerritory(safeRequest, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/internal/leads/{leadId}/reroute")
    public ResponseEntity<?> rerouteLead(@PathVariable Long leadId,
            @RequestBody(required = false) ManualRerouteLeadRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return unauthorizedResponse();
        }

        var safeRequest = request != null ? request : ManualRerouteLeadRequest.builder().build();
        var response = territoryUseCase.rerouteLead(leadId, safeRequest, tenantId, userId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
