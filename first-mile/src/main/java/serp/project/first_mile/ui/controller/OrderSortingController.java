/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.request.SortInboundOrdersRequest;
import serp.project.first_mile.dto.response.InboundOrderResponse;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.OrderSortingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inbound-orders")
@RequiredArgsConstructor
@Slf4j
public class OrderSortingController {

    private final AuthUtils authUtils;
    private final OrderSortingService orderSortingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<List<InboundOrderResponse>> getInboundOrders(
            @RequestParam(name = "post_office_code") String postOfficeCode,
            @RequestParam(name = "status", required = false) OrderStatus status) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<List<InboundOrderResponse>>builder()
                .result(orderSortingService.getInboundOrders(postOfficeCode, status, tenantId))
                .build();
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<List<InboundOrderResponse>> confirmInboundOrders(
            @RequestBody SortInboundOrdersRequest request) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<List<InboundOrderResponse>>builder()
                .result(orderSortingService.confirmInbound(request, tenantId))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
