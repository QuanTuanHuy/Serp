/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.AutoPlanBagDistributionRequest;
import serp.project.second_mile.dto.request.BagDistributionManifestFilterRequest;
import serp.project.second_mile.dto.request.ConfirmBagDistributionInboundRequest;
import serp.project.second_mile.dto.request.CreateBagDistributionManifestRequest;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.dto.response.BagDistributionManifestResponse;
import serp.project.second_mile.dto.response.BagDistributionPlanResponse;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagDistributionManifestStatus;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.BagDistributionManifestService;

@RestController
@RequestMapping("/api/v1/bag-distribution-manifests")
@RequiredArgsConstructor
public class BagDistributionManifestController {
    private final BagDistributionManifestService bagDistributionManifestService;
    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<PageResponse<BagDistributionManifestResponse>> listManifests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "origin_hub_id", required = false) Long originHubId,
            @RequestParam(name = "destination_type", required = false) BagDestinationType destinationType,
            @RequestParam(name = "destination_hub_id", required = false) Long destinationHubId,
            @RequestParam(name = "destination_post_office_code", required = false) String destinationPostOfficeCode,
            @RequestParam(name = "route_id", required = false) Long routeId,
            @RequestParam(name = "vehicle_id", required = false) Long vehicleId,
            @RequestParam(name = "assigned_driver_id", required = false) Long assignedDriverId,
            @RequestParam(required = false) BagDistributionManifestStatus status
    ) {
        BagDistributionManifestFilterRequest filterRequest = BagDistributionManifestFilterRequest.builder()
                .originHubId(originHubId)
                .destinationType(destinationType)
                .destinationHubId(destinationHubId)
                .destinationPostOfficeCode(destinationPostOfficeCode)
                .routeId(routeId)
                .vehicleId(vehicleId)
                .assignedDriverId(assignedDriverId)
                .status(status)
                .build();
        return ApiResponse.<PageResponse<BagDistributionManifestResponse>>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.list"))
                .result(bagDistributionManifestService.listManifests(page, size, filterRequest))
                .build();
    }

    @GetMapping("/{manifestId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<BagDistributionManifestResponse> getManifest(@PathVariable Long manifestId) {
        return ApiResponse.<BagDistributionManifestResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.detail"))
                .result(bagDistributionManifestService.getManifest(manifestId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagDistributionManifestResponse> createManifest(
            @Valid @RequestBody CreateBagDistributionManifestRequest request
    ) {
        return ApiResponse.<BagDistributionManifestResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.create"))
                .result(bagDistributionManifestService.createManifest(request))
                .build();
    }

    @PostMapping("/auto-plan")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagDistributionPlanResponse> autoPlan(
            @Valid @RequestBody AutoPlanBagDistributionRequest request
    ) {
        return ApiResponse.<BagDistributionPlanResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.auto_plan"))
                .result(bagDistributionManifestService.autoPlan(request))
                .build();
    }

    @PostMapping("/{manifestId}/confirm-outbound")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BagDistributionManifestResponse> confirmOutbound(@PathVariable Long manifestId) {
        return ApiResponse.<BagDistributionManifestResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.confirm_outbound"))
                .result(bagDistributionManifestService.confirmOutbound(manifestId))
                .build();
    }

    @PostMapping("/{manifestId}/confirm-inbound")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BagDistributionManifestResponse> confirmInbound(
            @PathVariable Long manifestId,
            @RequestBody(required = false) ConfirmBagDistributionInboundRequest request
    ) {
        return ApiResponse.<BagDistributionManifestResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.confirm_inbound"))
                .result(bagDistributionManifestService.confirmInbound(manifestId, request))
                .build();
    }

    @PostMapping(value = "/{manifestId}/driver-checkin-start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<BagDistributionManifestResponse> driverCheckinStart(
            @PathVariable Long manifestId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(name = "location_label", required = false) String locationLabel,
            @RequestParam("photo") MultipartFile photo
    ) {
        DriverHandoverCheckinRequest request = new DriverHandoverCheckinRequest(latitude, longitude, locationLabel);
        return ApiResponse.<BagDistributionManifestResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.driver_checkin_start"))
                .result(bagDistributionManifestService.driverCheckinStart(manifestId, request, photo))
                .build();
    }

    @PostMapping(value = "/{manifestId}/driver-checkin-end", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<BagDistributionManifestResponse> driverCheckinEnd(
            @PathVariable Long manifestId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(name = "location_label", required = false) String locationLabel,
            @RequestParam("photo") MultipartFile photo
    ) {
        DriverHandoverCheckinRequest request = new DriverHandoverCheckinRequest(latitude, longitude, locationLabel);
        return ApiResponse.<BagDistributionManifestResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.driver_checkin_end"))
                .result(bagDistributionManifestService.driverCheckinEnd(manifestId, request, photo))
                .build();
    }

    @PostMapping("/{manifestId}/cancel")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagDistributionManifestResponse> cancel(@PathVariable Long manifestId) {
        return ApiResponse.<BagDistributionManifestResponse>builder()
                .message(messageService.getMessage("success.bag_distribution_manifests.cancel"))
                .result(bagDistributionManifestService.cancel(manifestId))
                .build();
    }
}
