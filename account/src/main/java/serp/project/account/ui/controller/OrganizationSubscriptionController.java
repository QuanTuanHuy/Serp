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
import serp.project.account.core.domain.dto.request.*;
import serp.project.account.core.usecase.OrganizationSubscriptionUseCase;
import serp.project.account.kernel.utils.AuthUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/subscriptions")
@Slf4j
public class OrganizationSubscriptionController {

    private final OrganizationSubscriptionUseCase organizationSubscriptionUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping("/organizations/{organizationId}/subscribe")
    public ResponseEntity<?> subscribe(
            @PathVariable Long organizationId,
            @Valid @RequestBody SubscribeRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        if (requestedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("POST /api/v1/subscriptions/organizations/{}/subscribe - Subscribing to plan {}",
                organizationId, request.getPlanId());
        var response = organizationSubscriptionUseCase.subscribe(organizationId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/organizations/{organizationId}/trial")
    public ResponseEntity<?> startTrial(
            @PathVariable Long organizationId,
            @RequestParam Long planId) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        if (requestedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("POST /api/v1/subscriptions/organizations/{}/trial - Starting trial for plan {}",
                organizationId, planId);
        var response = organizationSubscriptionUseCase.startTrial(organizationId, planId, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/activate")
    public ResponseEntity<?> activateSubscription(@PathVariable Long subscriptionId) {
        Long activatedBy = authUtils.getCurrentUserId().orElse(null);
        if (activatedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscriptions/{}/activate - Activating subscription", subscriptionId);
        var response = organizationSubscriptionUseCase.activateSubscription(subscriptionId, activatedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/reject")
    public ResponseEntity<?> rejectSubscription(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody RejectSubscriptionRequest request) {
        Long rejectedBy = authUtils.getCurrentUserId().orElse(null);
        if (rejectedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscriptions/{}/reject - Rejecting subscription", subscriptionId);
        var response = organizationSubscriptionUseCase.rejectSubscription(subscriptionId, request, rejectedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/upgrade")
    public ResponseEntity<?> upgradeSubscription(
            @Valid @RequestBody UpgradeSubscriptionRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        if (requestedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        if (!authUtils.hasAnyRole("ORG_OWNER", "ORG_ADMIN", "SUPER_ADMIN", "SYSTEM_MODERATOR")) {
            var response = responseUtils.forbidden("You don't have permission to upgrade subscription");
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscriptions/{}/upgrade - Upgrading to plan {}",
                tenantId, request.getNewPlanId());
        var response = organizationSubscriptionUseCase.upgradeSubscription(tenantId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/downgrade")
    public ResponseEntity<?> downgradeSubscription(
            @Valid @RequestBody DowngradeSubscriptionRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        if (requestedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        if (!authUtils.hasAnyRole("ORG_OWNER", "ORG_ADMIN", "SUPER_ADMIN", "SYSTEM_MODERATOR")) {
            var response = responseUtils.forbidden("You don't have permission to downgrade subscription");
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscriptions/{}/downgrade - Downgrading to plan {}",
                tenantId, request.getNewPlanId());
        var response = organizationSubscriptionUseCase.downgradeSubscription(tenantId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/cancel")
    public ResponseEntity<?> cancelSubscription(
            @Valid @RequestBody CancelSubscriptionRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        if (requestedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        if (!authUtils.hasAnyRole("ORG_OWNER", "ORG_ADMIN", "SUPER_ADMIN", "SYSTEM_MODERATOR")) {
            var response = responseUtils.forbidden("You don't have permission to cancel subscription");
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscriptions/cancel - Cancelling subscription");
        var response = organizationSubscriptionUseCase.cancelSubscription(tenantId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/renew")
    public ResponseEntity<?> renewSubscription() {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        if (requestedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        if (!authUtils.hasAnyRole("ORG_OWNER", "ORG_ADMIN", "SUPER_ADMIN", "SYSTEM_MODERATOR")) {
            var response = responseUtils.forbidden("You don't have permission to renew subscription");
            return ResponseEntity.status(response.getCode()).body(response);
        }
        
        Long tenantId = authUtils.getCurrentTenantId().orElse(null);
        if (tenantId == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscriptions/renew - Renewing subscription");
        var response = organizationSubscriptionUseCase.renewSubscription(tenantId, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/extend-trial")
    public ResponseEntity<?> extendTrial(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody ExtendTrialRequest request) {
        Long requestedBy = authUtils.getCurrentUserId().orElse(null);
        if (requestedBy == null) {
            var response = responseUtils.unauthorized(Constants.ErrorMessage.UNAUTHORIZED);
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("PUT /api/v1/subscriptions/{}/extend-trial - Extending trial", subscriptionId);
        var response = organizationSubscriptionUseCase.extendTrial(subscriptionId, request, requestedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/expire")
    public ResponseEntity<?> expireSubscription(@PathVariable Long subscriptionId) {
        log.info("PUT /api/v1/subscriptions/{}/expire - Expiring subscription", subscriptionId);
        var response = organizationSubscriptionUseCase.expireSubscription(subscriptionId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/{organizationId}/active")
    public ResponseEntity<?> getActiveSubscription(@PathVariable Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/subscriptions/organizations/{}/active - Fetching active subscription", organizationId);
        var response = organizationSubscriptionUseCase.getActiveSubscription(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<?> getSubscriptionById(@PathVariable Long subscriptionId) {
        log.info("GET /api/v1/subscriptions/{} - Fetching subscription", subscriptionId);
        var response = organizationSubscriptionUseCase.getSubscriptionById(subscriptionId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/organizations/{organizationId}/history")
    public ResponseEntity<?> getSubscriptionHistory(@PathVariable Long organizationId) {
        if (!authUtils.canAccessOrganization(organizationId)) {
            var response = responseUtils.forbidden("You don't have permission to access this organization");
            return ResponseEntity.status(response.getCode()).body(response);
        }

        log.info("GET /api/v1/subscriptions/organizations/{}/history - Fetching subscription history", organizationId);
        var response = organizationSubscriptionUseCase.getSubscriptionHistory(organizationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
