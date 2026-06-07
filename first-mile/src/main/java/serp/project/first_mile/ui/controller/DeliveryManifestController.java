/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.request.ConfirmDeliveryFailureRequest;
import serp.project.first_mile.dto.request.ConfirmDeliveryRequest;
import serp.project.first_mile.dto.request.CreateDeliveryManifestRequest;
import serp.project.first_mile.dto.request.ReturnToSenderRequest;
import serp.project.first_mile.dto.response.DeliveryManifestResponse;
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
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
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
            @RequestParam(name = "post_office_code") String postOfficeCode,
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
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> startDelivery(@PathVariable Long id) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.startDelivery(id, tenantId))
                .build();
    }

    @PostMapping("/{manifestId}/orders/{orderCode}/delivered")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<DeliveryManifestResponse> confirmDelivered(
            @PathVariable Long manifestId,
            @PathVariable String orderCode,
            @RequestBody ConfirmDeliveryRequest request) {
        Long tenantId = getCurrentTenantId();
        return ApiResponse.<DeliveryManifestResponse>builder()
                .result(deliveryManifestService.confirmDelivered(manifestId, orderCode, request, tenantId))
                .build();
    }

    @PostMapping("/{manifestId}/orders/{orderCode}/failed")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
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
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
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
