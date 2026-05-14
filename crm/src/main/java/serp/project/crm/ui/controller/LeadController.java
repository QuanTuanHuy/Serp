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
import serp.project.crm.core.domain.dto.request.AssignLeadRequest;
import serp.project.crm.core.domain.dto.request.BulkAssignLeadRequest;
import serp.project.crm.core.domain.dto.request.CreateLeadRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadStatusRequest;
import serp.project.crm.core.domain.dto.request.UpdateLeadRequest;
import serp.project.crm.core.usecase.ActivityUseCase;
import serp.project.crm.core.usecase.LeadUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadController {

    private final LeadUseCase leadUseCase;
    private final ActivityUseCase activityUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createLead(@Valid @RequestBody CreateLeadRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = leadUseCase.createLead(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadRequest request) {
        Long userId = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (userId == null || tenantId == null) {
            return null;
        }
        var response = leadUseCase.updateLead(id, userId, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateLeadStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadStatusRequest request) {
        Long userId = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (userId == null || tenantId == null) {
            return null;
        }

        var response = leadUseCase.updateLeadStatus(id, userId, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeadById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }
        var response = leadUseCase.getLeadById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<?> getActivitiesByLeadId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();
        var response = activityUseCase.getActivitiesByLead(id, tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllLeads(
            @ModelAttribute LeadFilterRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        LeadFilterRequest safeRequest = request != null ? request : LeadFilterRequest.builder().build();
        var response = leadUseCase.filterLeads(safeRequest, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<?> filterLeads(@RequestBody(required = false) LeadFilterRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        LeadFilterRequest safeRequest = request != null ? request : LeadFilterRequest.builder().build();

        var response = leadUseCase.filterLeads(safeRequest, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignLead(
            @PathVariable Long id,
            @Valid @RequestBody AssignLeadRequest request) {
        Long userId = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (userId == null || tenantId == null) {
            return null;
        }

        var response = leadUseCase.assignLead(id, request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/bulk-assign")
    public ResponseEntity<?> bulkAssignLeads(@Valid @RequestBody BulkAssignLeadRequest request) {
        Long userId = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (userId == null || tenantId == null) {
            return null;
        }

        var response = leadUseCase.bulkAssignLeads(request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = leadUseCase.deleteLead(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
