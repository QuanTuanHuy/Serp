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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ContactController {

    private final ContactUseCase contactUseCase;
    private final AuthUtils authUtils;

    @PostMapping("/customers/{customerId}/contacts")
    public ResponseEntity<?> createContact(
            @PathVariable Long customerId,
            @Valid @RequestBody CreateContactRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        log.info("POST /api/v1/customers/{}/contacts - Creating contact for tenant: {}", customerId, tenantId);
        request.setCustomerId(customerId);
        var response = contactUseCase.createContact(request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/customers/{customerId}/contacts/{id}")
    public ResponseEntity<?> updateContact(
            @PathVariable Long customerId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }
        log.info("PATCH /api/v1/customers/{}/contacts/{} - Updating contact for tenant: {}", customerId, id, tenantId);
        var response = contactUseCase.updateContact(id, request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/customers/{customerId}/contacts/{id}")
    public ResponseEntity<?> getContactById(
            @PathVariable Long customerId,
            @PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        log.info("GET /api/v1/customers/{}/contacts/{} - Fetching contact for tenant: {}", customerId, id, tenantId);
        var response = contactUseCase.getContactById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/customers/{customerId}/contacts")
    public ResponseEntity<?> getAllContactsByCustomer(
            @PathVariable Long customerId) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        log.info("GET /api/v1/customers/{}/contacts - Fetching all contacts for tenant: {}", customerId, tenantId);

        var response = contactUseCase.getContactsByCustomerId(customerId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/customers/{customerId}/contacts/{id}")
    public ResponseEntity<?> deleteContact(
            @PathVariable Long customerId,
            @PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        log.info("DELETE /api/v1/customers/{}/contacts/{} - Deleting contact for tenant: {}", customerId, id, tenantId);
        var response = contactUseCase.deleteContact(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/contacts")
    public ResponseEntity<?> getAllContacts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);

        log.info("GET /api/v1/contacts - Fetching all contacts for tenant: {}, page: {}, size: {}", tenantId,
                page, size);

        PageRequest pageRequest = PageRequest.builder()
                .page(page)
                .size(size)
                .build();

        var response = contactUseCase.getAllContacts(tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
