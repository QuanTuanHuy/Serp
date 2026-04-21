/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.response.PickupTrackingOverviewResponse;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.PickupTrackingService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/pickup-tracking")
@RequiredArgsConstructor
public class PickupTrackingController {

    private final PickupTrackingService pickupTrackingService;
    private final MessageService messageService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<PickupTrackingOverviewResponse> getPickupTrackingOverview(
            @RequestParam(name = "trip_date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tripDate,
            @RequestParam(name = "post_office_id", required = false) Long postOfficeId,
            @RequestParam(name = "courier_staff_id", required = false) Long courierStaffId
    ) {
        return ApiResponse.<PickupTrackingOverviewResponse>builder()
                .message(messageService.getMessage("success.pickup_tracking.overview"))
                .result(pickupTrackingService.getPickupTrackingOverview(
                        tripDate,
                        postOfficeId,
                        courierStaffId
                ))
                .build();
    }
}
