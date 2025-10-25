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
import serp.project.account.core.domain.dto.request.*;
import serp.project.account.core.usecase.SubscriptionUseCase;
import serp.project.account.kernel.utils.AuthUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscriptions")
@Slf4j
public class SubscriptionController {

    private final SubscriptionUseCase organizationSubscriptionUseCase;
    private final AuthUtils authUtils;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @Valid @RequestBody SubscribeRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);

        log.info("POST /api/v1/subscriptions/subscribe - Subscribing to plan {}",
                tenantId, request.getPlanId());
        var response = organizationSubscriptionUseCase.subscribe(tenantId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/trial")
    public ResponseEntity<?> startTrial(
            @RequestParam Long planId) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);

        log.info("POST /api/v1/subscriptions/trial - Starting trial for plan {}",
                organizationId, planId);
        var response = organizationSubscriptionUseCase.startTrial(organizationId, planId, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/upgrade")
    public ResponseEntity<?> upgradeSubscription(
            @Valid @RequestBody UpgradeSubscriptionRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);

        log.info("PUT /api/v1/subscriptions/upgrade - Upgrading to plan {}",
                request.getNewPlanId());
        var response = organizationSubscriptionUseCase.upgradeSubscription(tenantId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/downgrade")
    public ResponseEntity<?> downgradeSubscription(
            @Valid @RequestBody DowngradeSubscriptionRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);

        log.info("PUT /api/v1/subscriptions/downgrade - Downgrading to plan {}",
                request.getNewPlanId());
        var response = organizationSubscriptionUseCase.downgradeSubscription(tenantId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/cancel")
    public ResponseEntity<?> cancelSubscription(
            @Valid @RequestBody CancelSubscriptionRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);

        log.info("PUT /api/v1/subscriptions/cancel - Cancelling subscription");
        var response = organizationSubscriptionUseCase.cancelSubscription(tenantId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/renew")
    public ResponseEntity<?> renewSubscription() {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);

        log.info("PUT /api/v1/subscriptions/renew - Renewing subscription");
        var response = organizationSubscriptionUseCase.renewSubscription(tenantId, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/extend-trial")
    public ResponseEntity<?> extendTrial(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody ExtendTrialRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);

        log.info("PUT /api/v1/subscriptions/{}/extend-trial - Extending trial", subscriptionId);
        var response = organizationSubscriptionUseCase.extendTrial(subscriptionId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/me/active")
    public ResponseEntity<?> getActiveSubscription() {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);

        log.info("GET /api/v1/subscriptions/me/active - Fetching active subscription for organization {}",
                organizationId);
        var response = organizationSubscriptionUseCase.getActiveSubscription(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionById(@PathVariable Long subscriptionId) {
        log.info("GET /api/v1/subscriptions/{} - Fetching subscription", subscriptionId);
        var response = organizationSubscriptionUseCase.getSubscriptionById(subscriptionId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/me/history")
    public ResponseEntity<?> getSubscriptionHistory() {
        Long organizationId = authUtils.getCurrentTenantId().orElse(null);

        log.info("GET /api/v1/subscriptions/me/history - Fetching subscription history for organization {}",
                organizationId);
        var response = organizationSubscriptionUseCase.getSubscriptionHistory(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
