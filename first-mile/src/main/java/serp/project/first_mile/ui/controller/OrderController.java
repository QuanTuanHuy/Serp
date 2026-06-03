package serp.project.first_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.OrderTimelineResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final AuthUtils authUtils;
    private final OrderService orderService;

    @GetMapping("/{orderId}/drop-off-post-office-suggestions")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public List<OrderDropOffPostOfficeSuggestionResponse> getDropOffPostOfficeSuggestions(
            @PathVariable Long orderId,
            @RequestParam(name = "limit", defaultValue = "5") Integer limit
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to get drop-off post office suggestions for Order {} tenant {}", orderId, tenantId);
        return orderService.getDropOffPostOfficeSuggestions(orderId, limit, tenantId);
    }

    @GetMapping("/{orderId}/timeline")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public List<OrderTimelineResponse> getOrderTimeline(@PathVariable Long orderId) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to get Order timeline {} for tenant {}", orderId, tenantId);
        return orderService.getOrderTimeline(orderId, tenantId);
    }

    @PostMapping(value = "/{orderId}/pickup-checkin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public PickupCheckinResponse pickupCheckinOrder(
            @PathVariable Long orderId,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("photo") MultipartFile photo
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to pickup-checkin Order {} for tenant {}", orderId, tenantId);
        return orderService.checkInPickupOrder(orderId, latitude, longitude, photo, tenantId);
    }

    // API test đồng bộ đơn
    @PostMapping("/{orderCode}/publish-order-event")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public void publishOrderEvent(@PathVariable String orderCode) {
        log.info("REST request to publish order event for order code {}", orderCode);
        orderService.publishOrderEvent(orderCode);
    }
}
