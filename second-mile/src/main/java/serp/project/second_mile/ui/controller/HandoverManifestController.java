/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.PageResponse;
import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.request.DriverHandoverCheckinRequest;
import serp.project.second_mile.dto.request.HandoverManifestFilterRequest;
import serp.project.second_mile.dto.response.HandoverManifestResponse;
import serp.project.second_mile.enums.HandoverManifestStatus;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.kafka.event.HandoverManifestSyncEvent;
import serp.project.second_mile.service.HandoverManifestService;

@RestController
@RequestMapping("/api/v1/handover-manifests")
@RequiredArgsConstructor
public class HandoverManifestController {
    private final HandoverManifestService handoverManifestService;
    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<PageResponse<HandoverManifestResponse>> listManifests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "origin_post_office_code", required = false) String originPostOfficeCode,
            @RequestParam(name = "target_hub_id", required = false) Long targetHubId,
            @RequestParam(name = "vehicle_id", required = false) Long vehicleId,
            @RequestParam(required = false) HandoverManifestStatus status
    ) {
        HandoverManifestFilterRequest filterRequest = HandoverManifestFilterRequest.builder()
                .originPostOfficeCode(originPostOfficeCode)
                .targetHubId(targetHubId)
                .vehicleId(vehicleId)
                .status(status)
                .build();
        return ApiResponse.<PageResponse<HandoverManifestResponse>>builder()
                .message(messageService.getMessage("success.handover_manifests.list"))
                .result(handoverManifestService.listManifests(page, size, filterRequest))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<HandoverManifestResponse> createManifest(@Valid @RequestBody CreateHandoverManifestRequest request) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.create"))
                .result(handoverManifestService.createManifest(request))
                .build();
    }

    @PostMapping("/internal/validate-outbound-sync")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<Void> validateOutboundSync(@RequestBody HandoverManifestSyncEvent event) {
        handoverManifestService.validateOutboundSync(event);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.handover_manifests.validate_outbound_sync"))
                .build();
    }

    @PostMapping("/{manifestId}/confirm-outbound")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<HandoverManifestResponse> confirmOutbound(@PathVariable Long manifestId) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.confirm_outbound"))
                .result(handoverManifestService.confirmOutbound(manifestId))
                .build();
    }

    @PostMapping("/{manifestId}/confirm-inbound")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<HandoverManifestResponse> confirmInbound(
            @PathVariable Long manifestId,
            @RequestBody(required = false) ConfirmHandoverInboundRequest request
    ) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.confirm_inbound"))
                .result(handoverManifestService.confirmInbound(manifestId, request))
                .build();
    }

    @PostMapping("/{manifestId}/driver-checkin-start")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<HandoverManifestResponse> driverCheckinStart(
            @PathVariable Long manifestId,
            @Valid @RequestBody DriverHandoverCheckinRequest request
    ) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.driver_checkin_start"))
                .result(handoverManifestService.driverCheckinStart(manifestId, request))
                .build();
    }

    @PostMapping("/{manifestId}/driver-checkin-end")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<HandoverManifestResponse> driverCheckinEnd(
            @PathVariable Long manifestId,
            @Valid @RequestBody DriverHandoverCheckinRequest request
    ) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.driver_checkin_end"))
                .result(handoverManifestService.driverCheckinEnd(manifestId, request))
                .build();
    }

    @GetMapping("/{manifestId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE', 'TMS_HUB_DRIVER')")
    public ApiResponse<HandoverManifestResponse> getManifest(@PathVariable Long manifestId) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.detail"))
                .result(handoverManifestService.getManifest(manifestId))
                .build();
    }
}
