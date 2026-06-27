/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_order.dto.ApiResponse;
import serp.project.tms_order.dto.request.ConfirmOrderPaymentRequest;
import serp.project.tms_order.dto.request.InitiateOrderPaymentRequest;
import serp.project.tms_order.dto.response.OrderPaymentConfirmResponse;
import serp.project.tms_order.dto.response.OrderPaymentInitResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.exception.MessageService;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.service.OrderPaymentService;

@RestController
@RequestMapping("/api/v1/order-payments")
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentController {

    private final AuthUtils authUtils;
    private final MessageService messageService;
    private final OrderPaymentService orderPaymentService;

    @PostMapping("/{orderId}/initiate")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<OrderPaymentInitResponse> initiateOrderPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) InitiateOrderPaymentRequest request
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to initiate payment for TMS Order {} tenant {}", orderId, tenantId);
        return ApiResponse.<OrderPaymentInitResponse>builder()
                .message(messageService.getMessage("success.orders.payment.initiate"))
                .result(orderPaymentService.initiateOrderPayment(orderId, tenantId, request))
                .build();
    }

    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<OrderPaymentConfirmResponse> confirmOrderPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody ConfirmOrderPaymentRequest request
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to confirm payment for TMS Order {} tenant {} appTransId={}",
                orderId,
                tenantId,
                request.getAppTransId());
        return ApiResponse.<OrderPaymentConfirmResponse>builder()
                .message(messageService.getMessage("success.orders.payment.confirm"))
                .result(orderPaymentService.confirmOrderPayment(orderId, tenantId, request))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
