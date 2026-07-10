/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.request.AutoAssignDeliveryPlanRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryPaymentRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.ManualAssignDeliveryOrdersRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.request.ScanOutDeliveryOrderRequest;
import serp.project.first_mile.dto.response.DeliveryAssignmentResponse;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentConfirmResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentInitResponse;
import serp.project.first_mile.dto.response.DeliveryScanOutResponse;
import serp.project.first_mile.dto.response.PickupOptimizationResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.enums.PickupShift;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.DeliveryDispatchService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/delivery-dispatch")
@RequiredArgsConstructor
public class DeliveryDispatchController {

    private final DeliveryDispatchService deliveryDispatchService;
    private final MessageService messageService;
    private final AuthUtils authUtils;

    @PostMapping("/plan")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PickupOptimizationResponse> optimizeDeliveryPlan(
            @Valid @RequestBody AutoAssignDeliveryPlanRequest request
    ) {
        return ApiResponse.<PickupOptimizationResponse>builder()
                .message(messageService.getMessage("success.delivery_dispatch.plan"))
                .result(deliveryDispatchService.optimizeDeliveryPlan(request))
                .build();
    }

    @PostMapping("/auto-assign")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<DeliveryAssignmentResponse> autoAssignDeliveryPlan(
            @Valid @RequestBody AutoAssignDeliveryPlanRequest request
    ) {
        return ApiResponse.<DeliveryAssignmentResponse>builder()
                .message(messageService.getMessage("success.delivery_dispatch.auto_assign"))
                .result(deliveryDispatchService.autoAssignDeliveryPlan(request))
                .build();
    }

    @PostMapping("/manual-assign")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<DeliveryAssignmentResponse> manualAssignDeliveryOrders(
            @Valid @RequestBody ManualAssignDeliveryOrdersRequest request,
            @RequestParam(value = "force_assign", required = false) Boolean forceAssign
    ) {
        if (Boolean.TRUE.equals(forceAssign)) {
            request.setForceAssign(true);
        }

        return ApiResponse.<DeliveryAssignmentResponse>builder()
                .message(messageService.getMessage("success.delivery_dispatch.manual_assign"))
                .result(deliveryDispatchService.manualAssignDeliveryOrders(request))
                .build();
    }

    @GetMapping("/trips")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<DeliveryAssignmentResponse> getDeliveryTrips(
            @RequestParam(name = "post_office_id", required = false) Long postOfficeId,
            @RequestParam(name = "shift", required = false) PickupShift shift,
            @RequestParam(name = "trip_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tripDate
    ) {
        return ApiResponse.<DeliveryAssignmentResponse>builder()
                .message(messageService.getMessage("success.delivery_dispatch.trips"))
                .result(deliveryDispatchService.getDeliveryTrips(postOfficeId, shift, tripDate))
                .build();
    }

    @PostMapping("/trips/{tripId}/scan-out")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<DeliveryScanOutResponse> scanOutDeliveryOrder(
            @PathVariable Long tripId,
            @Valid @RequestBody ScanOutDeliveryOrderRequest request
    ) {
        return ApiResponse.<DeliveryScanOutResponse>builder()
                .message(messageService.getMessage("success.delivery_dispatch.scan_out"))
                .result(deliveryDispatchService.scanOutDeliveryOrder(tripId, request))
                .build();
    }

    @PostMapping("/trips/{tripId}/orders/{orderCode}/payment/initiate")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryPaymentInitResponse> initiateTripDeliveryPayment(
            @PathVariable Long tripId,
            @PathVariable String orderCode
    ) {
        return ApiResponse.<DeliveryPaymentInitResponse>builder()
                .result(deliveryDispatchService.initiateTripDeliveryPayment(tripId, orderCode, getCurrentTenantId()))
                .build();
    }

    @PostMapping("/trips/{tripId}/orders/{orderCode}/payment/confirm")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryPaymentConfirmResponse> confirmTripDeliveryPayment(
            @PathVariable Long tripId,
            @PathVariable String orderCode,
            @Valid @RequestBody ConfirmDeliveryPaymentRequest request
    ) {
        return ApiResponse.<DeliveryPaymentConfirmResponse>builder()
                .result(deliveryDispatchService.confirmTripDeliveryPayment(
                        tripId, orderCode, request, getCurrentTenantId()))
                .build();
    }

    @PostMapping(value = "/trips/{tripId}/orders/{orderCode}/delivered", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryAssignmentResponse> confirmTripDelivered(
            @PathVariable Long tripId,
            @PathVariable String orderCode,
            @RequestParam(name = "cod_collected", required = false) Long codCollected,
            @RequestParam(name = "shipping_fee_collected", required = false) Long shippingFeeCollected,
            @RequestParam(name = "latitude") Double latitude,
            @RequestParam(name = "longitude") Double longitude,
            @RequestParam(name = "note", required = false) String note,
            @RequestPart("photo") MultipartFile photo
    ) {
        ConfirmDeliveryRequest request = ConfirmDeliveryRequest.builder()
                .codCollected(codCollected)
                .shippingFeeCollected(shippingFeeCollected)
                .latitude(latitude)
                .longitude(longitude)
                .note(note)
                .build();
        return ApiResponse.<DeliveryAssignmentResponse>builder()
                .result(deliveryDispatchService.confirmTripDelivered(
                        tripId, orderCode, request, photo, getCurrentTenantId()))
                .build();
    }

    @PostMapping("/trips/{tripId}/orders/{orderCode}/failed")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryAssignmentResponse> confirmTripDeliveryFailed(
            @PathVariable Long tripId,
            @PathVariable String orderCode,
            @RequestBody ConfirmDeliveryFailureRequest request
    ) {
        return ApiResponse.<DeliveryAssignmentResponse>builder()
                .result(deliveryDispatchService.confirmTripDeliveryFailed(
                        tripId, orderCode, request, getCurrentTenantId()))
                .build();
    }

    @PostMapping("/trips/{tripId}/orders/{orderCode}/return")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryAssignmentResponse> returnTripOrderToSender(
            @PathVariable Long tripId,
            @PathVariable String orderCode,
            @RequestBody ReturnToSenderRequest request
    ) {
        return ApiResponse.<DeliveryAssignmentResponse>builder()
                .result(deliveryDispatchService.returnTripOrderToSender(
                        tripId, orderCode, request, getCurrentTenantId()))
                .build();
    }

    @PostMapping("/trips/{tripId}/complete")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> completeDeliveryTrip(@PathVariable Long tripId) {
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryDispatchService.completeDeliveryTrip(tripId, getCurrentTenantId()))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
