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
import serp.project.crm.core.domain.dto.request.ConvertLeadRequest;
import serp.project.crm.core.domain.dto.request.CreateLeadRequest;
import serp.project.crm.core.domain.dto.request.LeadFilterRequest;
import serp.project.crm.core.domain.dto.request.QualifyLeadRequest;
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

    @PatchMapping("/{id}")
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

        var response = leadUseCase.getAllLeads(tenantId, pageRequest);
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

    @PostMapping("/{id}/qualify")
    public ResponseEntity<?> qualifyLead(
            @PathVariable Long id,
            @Valid @RequestBody QualifyLeadRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        request.setLeadId(id);
        var response = leadUseCase.qualifyLead(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{id}/convert")
    public ResponseEntity<?> convertLead(
            @PathVariable Long id,
            @Valid @RequestBody ConvertLeadRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        request.setLeadId(id);
        var response = leadUseCase.convertLead(request, tenantId);
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
