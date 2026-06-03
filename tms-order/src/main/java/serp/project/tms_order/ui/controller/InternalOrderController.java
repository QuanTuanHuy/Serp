/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_order.dto.request.InternalOrderLookupRequest;
import serp.project.tms_order.dto.request.InternalOrderStatusTransitionRequest;
import serp.project.tms_order.dto.request.InternalPickupCandidateRequest;
import serp.project.tms_order.dto.response.OrderOperationView;
import serp.project.tms_order.dto.response.OrderStatusTransitionResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.service.OrderQueryService;
import serp.project.tms_order.service.OrderTransitionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/orders")
@RequiredArgsConstructor
@Slf4j
public class InternalOrderController {

    private final AuthUtils authUtils;
    private final OrderQueryService orderQueryService;
    private final OrderTransitionService orderTransitionService;

    @PostMapping("/lookup")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public List<OrderOperationView> lookupOrders(@RequestBody InternalOrderLookupRequest request) {
        Long tenantId = getCurrentTenantId();
        log.info("Internal request to lookup TMS orders tenant {}", tenantId);
        return orderQueryService.lookupOrders(request, tenantId);
    }

    @PostMapping("/pickup-candidates")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public List<OrderOperationView> findPickupCandidates(@RequestBody InternalPickupCandidateRequest request) {
        Long tenantId = getCurrentTenantId();
        log.info("Internal request to find pickup candidates tenant {}", tenantId);
        return orderQueryService.findPickupCandidates(request, tenantId);
    }

    @PostMapping("/status-transitions")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public OrderStatusTransitionResponse applyStatusTransitions(
            @Valid @RequestBody InternalOrderStatusTransitionRequest request
    ) {
        Long tenantId = getCurrentTenantId();
        log.info("Internal request to apply order transitions source={} idempotencyKey={} tenant {}",
                request.getSource(), request.getIdempotencyKey(), tenantId);
        return orderTransitionService.applyTransitions(request, tenantId);
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
