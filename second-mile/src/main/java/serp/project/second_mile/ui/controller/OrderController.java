/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.OrderFilterRequest;
import serp.project.second_mile.dto.response.OrderResponse;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.OrderManagementService;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderManagementService orderManagementService;
    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<PageResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "order_code", required = false) String orderCode,
            @RequestParam(name = "customer_order_code", required = false) String customerOrderCode,
            @RequestParam(name = "origin_post_office_code", required = false) String originPostOfficeCode,
            @RequestParam(name = "destination_post_office_code", required = false) String destinationPostOfficeCode,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(name = "assigned_to_bag", required = false) Boolean assignedToBag
    ) {
        OrderFilterRequest filterRequest = OrderFilterRequest.builder()
                .keyword(keyword)
                .orderCode(orderCode)
                .customerOrderCode(customerOrderCode)
                .originPostOfficeCode(originPostOfficeCode)
                .destinationPostOfficeCode(destinationPostOfficeCode)
                .status(status)
                .assignedToBag(assignedToBag)
                .build();
        return ApiResponse.<PageResponse<OrderResponse>>builder()
                .message(messageService.getMessage("success.orders.list"))
                .result(orderManagementService.getOrders(page, size, filterRequest))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        return ApiResponse.<OrderResponse>builder()
                .message(messageService.getMessage("success.orders.detail"))
                .result(orderManagementService.getOrderById(id))
                .build();
    }
}
