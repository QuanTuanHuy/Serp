/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_order.dto.ApiResponse;
import serp.project.tms_order.dto.request.ConfirmDropOffOrderRequest;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.exception.MessageService;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.service.OrderDropOffService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-drop-offs")
@RequiredArgsConstructor
@Slf4j
public class OrderDropOffController {

    private final AuthUtils authUtils;
    private final MessageService messageService;
    private final OrderDropOffService orderDropOffService;

    @GetMapping("/{orderId}/post-office-suggestions")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<List<OrderDropOffPostOfficeSuggestionResponse>> getDropOffPostOfficeSuggestions(
            @PathVariable Long orderId,
            @RequestParam(name = "limit", defaultValue = "5") Integer limit
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to get drop-off post office suggestions for TMS Order {} tenant {}", orderId, tenantId);
        return ApiResponse.<List<OrderDropOffPostOfficeSuggestionResponse>>builder()
                .message(messageService.getMessage("success.orders.drop_off_suggestions"))
                .result(orderDropOffService.getDropOffPostOfficeSuggestions(orderId, limit, tenantId))
                .build();
    }

    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<OrderConfirmationResponse> confirmDropOffOrderAtPostOffice(
            @PathVariable Long orderId,
            @Valid @RequestBody ConfirmDropOffOrderRequest request
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to confirm drop-off TMS Order {} at post office {} tenant {}",
                orderId, request.getPostOfficeId(), tenantId);
        return ApiResponse.<OrderConfirmationResponse>builder()
                .message(messageService.getMessage("success.orders.confirm"))
                .result(orderDropOffService.confirmDropOffOrderAtPostOffice(orderId, tenantId, request))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
