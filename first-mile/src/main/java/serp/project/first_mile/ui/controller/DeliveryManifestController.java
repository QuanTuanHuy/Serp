/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryPaymentRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.CreateDeliveryManifestRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentConfirmResponse;
import serp.project.first_mile.dto.response.DeliveryPaymentInitResponse;
import serp.project.first_mile.enums.DeliveryManifestStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.CodCollectionService;
import serp.project.first_mile.service.DeliveryManifestService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-manifests")
@RequiredArgsConstructor
@Slf4j
public class DeliveryManifestController {

    private final AuthUtils authUtils;
    private final DeliveryManifestService deliveryManifestService;
    private final CodCollectionService codCollectionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<DeliveryManifestResponse> createManifest(
            @RequestBody CreateDeliveryManifestRequest request) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.createManifest(request, tenantId))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<List<DeliveryManifestResponse>> getManifests(
            @RequestParam(name = "post_office_code", required = false) String postOfficeCode,
            @RequestParam(name = "status", required = false) DeliveryManifestStatus status,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<List<DeliveryManifestResponse>>builder()
                .result(deliveryManifestService.getManifests(postOfficeCode, status, date, tenantId))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> getManifest(@PathVariable Long id) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.getManifest(id, tenantId))
                .build();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> startDelivery(@PathVariable Long id) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.startDelivery(id, tenantId))
                .build();
    }

    @PostMapping("/{manifestId}/orders/{orderCode}/payment/initiate")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryPaymentInitResponse> initiateDeliveryPayment(
            @PathVariable Long manifestId,
            @PathVariable String orderCode
    ) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryPaymentInitResponse>builder()
                .result(deliveryManifestService.initiateDeliveryPayment(manifestId, orderCode, tenantId))
                .build();
    }

    @PostMapping("/{manifestId}/orders/{orderCode}/payment/confirm")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryPaymentConfirmResponse> confirmDeliveryPayment(
            @PathVariable Long manifestId,
            @PathVariable String orderCode,
            @Valid @RequestBody ConfirmDeliveryPaymentRequest request
    ) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryPaymentConfirmResponse>builder()
                .result(deliveryManifestService.confirmDeliveryPayment(manifestId, orderCode, request, tenantId))
                .build();
    }

    @PostMapping(value = "/{manifestId}/orders/{orderCode}/delivered", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> confirmDelivered(
            @PathVariable Long manifestId,
            @PathVariable String orderCode,
            @RequestParam(name = "cod_collected", required = false) Long codCollected,
            @RequestParam(name = "shipping_fee_collected", required = false) Long shippingFeeCollected,
            @RequestParam(name = "latitude") Double latitude,
            @RequestParam(name = "longitude") Double longitude,
            @RequestParam(name = "note", required = false) String note,
            @RequestPart("photo") MultipartFile photo) {
        Long tenantId = getCurrentTenantId();
        ConfirmDeliveryRequest request = ConfirmDeliveryRequest.builder()
                .codCollected(codCollected)
                .shippingFeeCollected(shippingFeeCollected)
                .latitude(latitude)
                .longitude(longitude)
                .note(note)
                .build();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.confirmDelivered(manifestId, orderCode, request, photo, tenantId))
                .build();
    }

    @PostMapping("/{manifestId}/orders/{orderCode}/failed")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> confirmFailed(
            @PathVariable Long manifestId,
            @PathVariable String orderCode,
            @RequestBody ConfirmDeliveryFailureRequest request) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.confirmFailed(manifestId, orderCode, request, tenantId))
                .build();
    }

    @PostMapping("/{manifestId}/orders/{orderCode}/return")
    @PreAuthorize("hasRole('TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> returnToSender(
            @PathVariable Long manifestId,
            @PathVariable String orderCode,
            @RequestBody ReturnToSenderRequest request) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.returnToSender(manifestId, orderCode, request, tenantId))
                .build();
    }

    @GetMapping("/{manifestId}/financial-summary")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> getFinancialSummary(@PathVariable Long manifestId) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(codCollectionService.getFinancialSummary(manifestId, tenantId))
                .build();
    }

    private Long getCurrentTenantId() {
        return authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
    }
}
