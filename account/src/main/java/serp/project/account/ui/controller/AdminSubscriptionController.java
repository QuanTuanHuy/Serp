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
@RequestMapping("/api/v1/admin/subscriptions")
@Slf4j
public class AdminSubscriptionController {
    private final SubscriptionUseCase subscriptionUseCase;

    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<?> getAllSubscriptions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String billingCycle) {
        GetSubscriptionParams params = GetSubscriptionParams.builder()
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .organizationId(organizationId)
                .status(status)
                .billingCycle(billingCycle)
                .build();

        log.info("GET /api/v1/admin/subscriptions - Getting all subscriptions");
        var response = subscriptionUseCase.getAllSubscriptions(params);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/activate")
    public ResponseEntity<?> activateSubscription(@PathVariable Long subscriptionId) {
        Long activatedBy = authUtils.getCurrentUserId().orElse(null);

        log.info("PUT /api/v1/admin/subscriptions/{}/activate - Activating subscription", subscriptionId);
        var response = subscriptionUseCase.activateSubscription(subscriptionId, activatedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/reject")
    public ResponseEntity<?> rejectSubscription(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody RejectSubscriptionRequest request) {
        Long rejectedBy = authUtils.getCurrentUserId().orElse(null);

        log.info("PUT /api/v1/subscriptions/{}/reject - Rejecting subscription", subscriptionId);
        var response = subscriptionUseCase.rejectSubscription(subscriptionId, request, rejectedBy);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{subscriptionId}/expire")
    public ResponseEntity<?> expireSubscription(@PathVariable Long subscriptionId) {
        log.info("PUT /api/v1/subscriptions/{}/expire - Expiring subscription", subscriptionId);
        var response = subscriptionUseCase.expireSubscription(subscriptionId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
