package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CancelOrderRequest;
import serp.project.first_mile.dto.request.ConfirmDropOffOrderRequest;
import serp.project.first_mile.dto.request.ConfirmOrderPaymentRequest;
import serp.project.first_mile.dto.request.CreateOrderRequest;
import serp.project.first_mile.dto.request.OrderFilterRequest;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.request.UpdateOrderRequest;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderPaymentConfirmResponse;
import serp.project.first_mile.dto.response.OrderPaymentInitResponse;
import serp.project.first_mile.dto.response.OrderDetailResponse;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.OrderTimelineResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final AuthUtils authUtils;
    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public PageResponse<OrderDetailResponse> getOrders(
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
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );

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

        log.info("REST request to get Order list for tenant {}", tenantId);
        return orderService.getOrders(page, size, filterRequest, tenantId);
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> exportTemplate() {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to export Order Template Excel for tenant {}", tenantId);

        byte[] excelData = orderService.exportTemplate(tenantId);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order_template.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelData);
    }

    @PostMapping("/validate")
    public ValidateImportFileDTO<OrderImportDTO> validateFile(
            @RequestParam("file") MultipartFile file
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
            () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to validate Order import file for tenant {}", tenantId);
        return orderService.validateImportFile(file, tenantId);
    }

    @PostMapping("/import")
    public ImportHistoryResponse importFile(
            @RequestParam("file") MultipartFile file
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to import Order file for tenant {}", tenantId);
        return orderService.importOrdersAsync(file, tenantId);
    }

    @PostMapping("/{orderId}/confirm")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public OrderConfirmationResponse confirmOrder(@PathVariable Long orderId) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to confirm Order {} for tenant {}", orderId, tenantId);
        return orderService.confirmOrder(orderId, tenantId);
    }

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

    @PostMapping("/{orderId}/drop-off-confirm")
    @PreAuthorize("hasRole('TMS_POSTOFFICER_MANAGER')")
    public OrderConfirmationResponse confirmDropOffOrderAtPostOffice(
            @PathVariable Long orderId,
            @Valid @RequestBody ConfirmDropOffOrderRequest request
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to confirm drop-off Order {} at post office {} tenant {}",
                orderId, request.getPostOfficeId(), tenantId);
        return orderService.confirmDropOffOrderAtPostOffice(orderId, request.getPostOfficeId(), tenantId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public OrderDetailResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to create Order for tenant {}", tenantId);
        return orderService.createOrder(request, tenantId);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public OrderDetailResponse getOrderById(@PathVariable Long orderId) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to get Order {} for tenant {}", orderId, tenantId);
        return orderService.getOrderById(orderId, tenantId);
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

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public OrderDetailResponse updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderRequest request
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to update Order {} for tenant {}", orderId, tenantId);
        return orderService.updateOrder(orderId, request, tenantId);
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public OrderDetailResponse cancelOrder(@PathVariable Long orderId, @RequestBody CancelOrderRequest request) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to cancel Order {} for tenant {}", orderId, tenantId);
        return orderService.cancelOrder(orderId, tenantId, request);
    }

    @PostMapping("/{orderId}/payment/initiate")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public OrderPaymentInitResponse initiateOrderPayment(@PathVariable Long orderId) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to initiate payment for Order {} tenant {}", orderId, tenantId);
        return orderService.initiateOrderPayment(orderId, tenantId);
    }

    @PostMapping("/{orderId}/payment/confirm")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_CUSTOMER')")
    public OrderPaymentConfirmResponse confirmOrderPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody ConfirmOrderPaymentRequest request
    ) {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to confirm payment for Order {} tenant {} appTransId={}",
                orderId,
                tenantId,
                request.getAppTransId());
        return orderService.confirmOrderPayment(orderId, tenantId, request);
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
