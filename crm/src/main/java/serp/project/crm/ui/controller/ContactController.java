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
import serp.project.crm.core.domain.dto.request.CreateContactRequest;
import serp.project.crm.core.domain.dto.request.UpdateContactRequest;
import serp.project.crm.core.usecase.ContactUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactUseCase contactUseCase;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createContact(@Valid @RequestBody CreateContactRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("POST /api/v1/contacts - Creating contact for tenant: {}", tenantId);
        var response = contactUseCase.createContact(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PUT /api/v1/contacts/{} - Updating contact for tenant: {}", id, tenantId);
        var response = contactUseCase.updateContact(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContactById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/contacts/{} - Fetching contact for tenant: {}", id, tenantId);
        var response = contactUseCase.getContactById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllContacts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("GET /api/v1/contacts - Fetching all contacts for tenant: {}, page: {}, size: {}", tenantId, page, size);
        
        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();
        
        var response = contactUseCase.getAllContacts(tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{id}/set-primary")
    public ResponseEntity<?> setPrimaryContact(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("PATCH /api/v1/contacts/{}/set-primary - Setting as primary for tenant: {}", id, tenantId);
        var response = contactUseCase.setPrimaryContact(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalArgumentException("Tenant ID not found in token"));
        
        log.info("DELETE /api/v1/contacts/{} - Deleting contact for tenant: {}", id, tenantId);
        var response = contactUseCase.deleteContact(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
