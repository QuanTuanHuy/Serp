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
import serp.project.crm.core.domain.dto.request.CreateAccountRequest;
import serp.project.crm.core.domain.dto.request.AccountFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateAccountRequest;
import serp.project.crm.core.domain.dto.request.UpdateCreditLimitRequest;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.AccountType;
import serp.project.crm.core.usecase.ActivityUseCase;
import serp.project.crm.core.usecase.AccountUseCase;
import serp.project.crm.kernel.utils.AuthUtils;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountUseCase accountUseCase;
    private final ActivityUseCase activityUseCase;

    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = accountUseCase.createAccount(request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = accountUseCase.updateAccount(id, request, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = accountUseCase.getAccountById(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<?> getActivitiesByAccountId(
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

        var response = activityUseCase.getActivitiesByAccount(id, tenantId, pageRequest);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) ActiveStatus activeStatus,
            @RequestParam(required = false) AccountType accountType,
            @RequestParam(required = false, name = "minRevenue") BigDecimal totalRevenueMin,
            @RequestParam(required = false, name = "maxRevenue") BigDecimal totalRevenueMax,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        AccountFilterRequest filter = AccountFilterRequest.builder()
                .keyword(keyword)
                .industries(industry != null ? List.of(industry) : null)
                .statuses(activeStatus != null ? List.of(activeStatus) : null)
                .accountType(accountType)
                .totalRevenueMin(totalRevenueMin)
                .totalRevenueMax(totalRevenueMax)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .build();

        var response = accountUseCase.filterAccounts(filter, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/search")
    public ResponseEntity<?> filterAccounts(@RequestBody(required = false) AccountFilterRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        AccountFilterRequest safeRequest = request != null ? request : AccountFilterRequest.builder().build();

        var response = accountUseCase.filterAccounts(safeRequest, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateAccount(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        var response = accountUseCase.activateAccount(id, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateAccount(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        var response = accountUseCase.deactivateAccount(id, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}/credit-limit")
    public ResponseEntity<?> updateCreditLimit(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCreditLimitRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        Long userId = authUtils.getCurrentUserId().orElse(null);
        if (tenantId == null || userId == null) {
            return null;
        }

        var response = accountUseCase.updateCreditLimit(id, request, userId, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            return null;
        }

        var response = accountUseCase.deleteAccount(id, tenantId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
