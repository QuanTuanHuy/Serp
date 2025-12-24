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
import serp.project.crm.core.domain.dto.request.CreateCustomerRequest;
import serp.project.crm.core.domain.dto.request.CustomerFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateCustomerRequest;
import serp.project.crm.core.usecase.ActivityUseCase;
import serp.project.crm.core.usecase.CustomerUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerUseCase customerUseCase;
    private final ActivityUseCase activityUseCase;

    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = customerUseCase.createCustomer(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = customerUseCase.updateCustomer(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = customerUseCase.getCustomerById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<?> getActivitiesByCustomerId(
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

        var response = activityUseCase.getActivitiesByCustomer(id, tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllCustomers(
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

        var response = customerUseCase.getAllCustomers(tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<?> filterCustomers(@RequestBody(required = false) CustomerFilterRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        CustomerFilterRequest safeRequest = request != null ? request : CustomerFilterRequest.builder().build();

        var response = customerUseCase.filterCustomers(safeRequest, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = customerUseCase.deleteCustomer(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
