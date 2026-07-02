/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.request.AutoAssignDeliveryPlanRequest;
import serp.project.first_mile.dto.request.ScanOutDeliveryOrderRequest;
import serp.project.first_mile.dto.response.DeliveryAssignmentResponse;
import serp.project.first_mile.dto.response.DeliveryScanOutResponse;
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
}
