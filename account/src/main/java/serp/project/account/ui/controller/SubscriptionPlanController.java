/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.AddModuleToPlanRequest;
import serp.project.account.core.domain.dto.request.CreateSubscriptionPlanRequest;
import serp.project.account.core.domain.dto.request.GetSubscriptionParams;
import serp.project.account.core.domain.dto.request.GetSubscriptionPlanParams;
import serp.project.account.core.domain.dto.request.UpdateSubscriptionPlanRequest;
import serp.project.account.core.usecase.SubscriptionPlanUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscription-plans")
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanUseCase subscriptionPlanUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<?> createPlan(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        Long createdBy = authUtils.getCurrentUserId().orElse(null);
        if (createdBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("POST /api/v1/subscription-plans - Creating plan");
        var response = subscriptionPlanUseCase.createPlan(request, createdBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{planId}")
    public ResponseEntity<?> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        Long updatedBy = authUtils.getCurrentUserId().orElse(null);
        if (updatedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscription-plans/{} - Updating plan", planId);
        var response = subscriptionPlanUseCase.updatePlan(planId, request, updatedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        log.info("DELETE /api/v1/subscription-plans/{} - Deleting plan", planId);
        var response = subscriptionPlanUseCase.deletePlan(planId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlanById(@PathVariable Long planId) {
        log.info("GET /api/v1/subscription-plans/{} - Fetching plan", planId);
        var response = subscriptionPlanUseCase.getPlanById(planId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/code/{planCode}")
    public ResponseEntity<?> getPlanByCode(@PathVariable String planCode) {
        log.info("GET /api/v1/subscription-plans/code/{} - Fetching plan by code", planCode);
        var response = subscriptionPlanUseCase.getPlanByCode(planCode);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllPlans(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) Boolean isCustom,
            @RequestParam(required = false) Long organizationId) {
        log.info("GET /api/v1/subscription-plans - Fetching all plans");
        if (!authUtils.isSystemAdmin()) {
            organizationId = null;
        }
        var params = GetSubscriptionPlanParams.builder()
                .page(page).pageSize(pageSize).sortBy(sortBy).sortDirection(sortDir)
                .isCustom(isCustom).organizationId(organizationId)
                .build();

        var response = subscriptionPlanUseCase.getAllPlans(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{planId}/modules")
    public ResponseEntity<?> addModuleToPlan(
            @PathVariable Long planId,
            @Valid @RequestBody AddModuleToPlanRequest request) {
        Long addedBy = authUtils.getCurrentUserId().orElse(null);
        if (addedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("POST /api/v1/subscription-plans/{}/modules - Adding module to plan", planId);
        var response = subscriptionPlanUseCase.addModuleToPlan(planId, request, addedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{planId}/modules/{moduleId}")
    public ResponseEntity<?> removeModuleFromPlan(
            @PathVariable Long planId,
            @PathVariable Long moduleId) {
        log.info("DELETE /api/v1/subscription-plans/{}/modules/{} - Removing module from plan", planId, moduleId);
        var response = subscriptionPlanUseCase.removeModuleFromPlan(planId, moduleId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{planId}/modules")
    public ResponseEntity<?> getPlanModules(@PathVariable Long planId) {
        log.info("GET /api/v1/subscription-plans/{}/modules - Fetching plan modules", planId);
        var response = subscriptionPlanUseCase.getPlanModules(planId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
