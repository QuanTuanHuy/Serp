/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import serp.project.tms_order.dto.ApiResponse;
import serp.project.tms_order.dto.PageResponse;
import serp.project.tms_order.dto.request.CancelOrderRequest;
import serp.project.tms_order.dto.request.ConfirmDropOffOrderRequest;
import serp.project.tms_order.dto.request.ConfirmOrderPaymentRequest;
import serp.project.tms_order.dto.request.CreateOrderRequest;
import serp.project.tms_order.dto.request.InitiateOrderPaymentRequest;
import serp.project.tms_order.dto.request.OrderImportDTO;
import serp.project.tms_order.dto.request.OrderFilterRequest;
import serp.project.tms_order.dto.request.UpdateOrderRequest;
import serp.project.tms_order.dto.response.ImportHistoryResponse;
import serp.project.tms_order.dto.response.OrderConfirmationResponse;
import serp.project.tms_order.dto.response.OrderDetailResponse;
import serp.project.tms_order.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.tms_order.dto.response.OrderPaymentConfirmResponse;
import serp.project.tms_order.dto.response.OrderPaymentInitResponse;
import serp.project.tms_order.dto.response.OrderTimelineResponse;
import serp.project.tms_order.dto.response.ValidateImportFileDTO;
import serp.project.tms_order.enums.OrderStatus;
import serp.project.tms_order.exception.AppException;
import serp.project.tms_order.exception.ErrorCode;
import serp.project.tms_order.exception.MessageService;
import serp.project.tms_order.kernel.utils.AuthUtils;
import serp.project.tms_order.service.OrderService;
import serp.project.tms_order.service.OrderTimelineService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final AuthUtils authUtils;
    private final MessageService messageService;
    private final OrderService orderService;
    private final OrderTimelineService orderTimelineService;

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ResponseEntity<byte[]> exportTemplate() {
        Long tenantId = getCurrentTenantId();
        log.info("REST request to export TMS Order Template Excel for tenant {}", tenantId);

        byte[] excelData = orderService.exportTemplate(tenantId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData);
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ValidateImportFileDTO<OrderImportDTO> validateFile(@RequestParam("file") MultipartFile file) {
        Long tenantId = getCurrentTenantId();
        log.info("REST request to validate TMS Order import file for tenant {}", tenantId);
        return orderService.validateImportFile(file, tenantId);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ImportHistoryResponse importFile(@RequestParam("file") MultipartFile file) {
        Long tenantId = getCurrentTenantId();
        log.info("REST request to import TMS Order file for tenant {}", tenantId);
        return orderService.importOrdersAsync(file, tenantId);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<PageResponse<OrderDetailResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "order_code", required = false) String orderCode,
            @RequestParam(name = "customer_order_code", required = false) String customerOrderCode,
            @RequestParam(name = "sender_phone", required = false) String senderPhone,
            @RequestParam(name = "receiver_phone", required = false) String receiverPhone,
            @RequestParam(name = "origin_post_office_code", required = false) String originPostOfficeCode,
            @RequestParam(name = "destination_post_office_code", required = false) String destinationPostOfficeCode,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(name = "is_confirm", required = false) Boolean isConfirm,
            @RequestParam(name = "created_from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(name = "created_to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(name = "pickup_from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupFrom,
            @RequestParam(name = "pickup_to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupTo
    ) {
        Long tenantId = getCurrentTenantId();
        OrderFilterRequest filterRequest = OrderFilterRequest.builder()
                .keyword(keyword)
                .orderCode(orderCode)
                .customerOrderCode(customerOrderCode)
                .senderPhone(senderPhone)
                .receiverPhone(receiverPhone)
                .originPostOfficeCode(originPostOfficeCode)
                .destinationPostOfficeCode(destinationPostOfficeCode)
                .status(status)
                .isConfirm(isConfirm)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .pickupFrom(pickupFrom)
                .pickupTo(pickupTo)
                .build();

        log.info("REST request to get TMS Order list for tenant {}", tenantId);
        return ApiResponse.<PageResponse<OrderDetailResponse>>builder()
                .message(messageService.getMessage("success.orders.list"))
                .result(orderService.getOrders(page, size, filterRequest, tenantId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<OrderDetailResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to create TMS Order for tenant {}", tenantId);
        return ApiResponse.<OrderDetailResponse>builder()
                .message(messageService.getMessage("success.orders.create"))
                .result(orderService.createOrder(request, tenantId))
                .build();
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<OrderDetailResponse> getOrderById(@PathVariable Long orderId) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to get TMS Order {} for tenant {}", orderId, tenantId);
        return ApiResponse.<OrderDetailResponse>builder()
                .message(messageService.getMessage("success.orders.detail"))
                .result(orderService.getOrderById(orderId, tenantId))
                .build();
    }

    @GetMapping("/{orderId}/timeline")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<List<OrderTimelineResponse>> getOrderTimeline(@PathVariable Long orderId) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to get TMS Order timeline {} for tenant {}", orderId, tenantId);
        orderService.getOrderById(orderId, tenantId);
        return ApiResponse.<List<OrderTimelineResponse>>builder()
                .message(messageService.getMessage("success.orders.timeline"))
                .result(orderTimelineService.getTimeline(orderId, tenantId))
                .build();
    }

    @GetMapping("/{orderId}/drop-off-post-office-suggestions")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<List<OrderDropOffPostOfficeSuggestionResponse>> getDropOffPostOfficeSuggestions(
            @PathVariable Long orderId,
            @RequestParam(name = "limit", defaultValue = "5") Integer limit
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to get drop-off post office suggestions for TMS Order {} tenant {}", orderId, tenantId);
        return ApiResponse.<List<OrderDropOffPostOfficeSuggestionResponse>>builder()
                .message(messageService.getMessage("success.orders.drop_off_suggestions"))
                .result(orderService.getDropOffPostOfficeSuggestions(orderId, limit, tenantId))
                .build();
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<OrderDetailResponse> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderRequest request
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to update TMS Order {} for tenant {}", orderId, tenantId);
        return ApiResponse.<OrderDetailResponse>builder()
                .message(messageService.getMessage("success.orders.update"))
                .result(orderService.updateOrder(orderId, request, tenantId))
                .build();
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<OrderDetailResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) CancelOrderRequest request
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to cancel TMS Order {} for tenant {}", orderId, tenantId);
        return ApiResponse.<OrderDetailResponse>builder()
                .message(messageService.getMessage("success.orders.cancel"))
                .result(orderService.cancelOrder(orderId, tenantId, request))
                .build();
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<Void> deleteOrder(@PathVariable Long orderId) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to delete TMS Order {} for tenant {}", orderId, tenantId);
        orderService.deleteOrder(orderId, tenantId);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.orders.delete"))
                .build();
    }

    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<OrderConfirmationResponse> confirmOrder(@PathVariable Long orderId) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to confirm TMS Order {} for tenant {}", orderId, tenantId);
        return ApiResponse.<OrderConfirmationResponse>builder()
                .message(messageService.getMessage("success.orders.confirm"))
                .result(orderService.confirmOrder(orderId, tenantId))
                .build();
    }

    @PostMapping("/{orderId}/drop-off-confirm")
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
                .result(orderService.confirmDropOffOrderAtPostOffice(orderId, tenantId, request))
                .build();
    }

    @PostMapping("/{orderId}/payment/initiate")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public ApiResponse<OrderPaymentInitResponse> initiateOrderPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) InitiateOrderPaymentRequest request
    ) {
        Long tenantId = getCurrentTenantId();

        log.info("REST request to initiate payment for TMS Order {} tenant {}", orderId, tenantId);
        return ApiResponse.<OrderPaymentInitResponse>builder()
                .message(messageService.getMessage("success.orders.payment.initiate"))
                .result(orderService.initiateOrderPayment(orderId, tenantId, request))
                .build();
    }

    @PostMapping("/{orderId}/payment/confirm")
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
                .result(orderService.confirmOrderPayment(orderId, tenantId, request))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
