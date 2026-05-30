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
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.request.ConfirmHandoverInboundRequest;
import serp.project.second_mile.dto.request.CreateHandoverManifestRequest;
import serp.project.second_mile.dto.response.HandoverManifestResponse;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.HandoverManifestService;

@RestController
@RequestMapping("/api/v1/handover-manifests")
@RequiredArgsConstructor
public class HandoverManifestController {
    private final HandoverManifestService handoverManifestService;
    private final MessageService messageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<HandoverManifestResponse> createManifest(@Valid @RequestBody CreateHandoverManifestRequest request) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.create"))
                .result(handoverManifestService.createManifest(request))
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

    @GetMapping("/{manifestId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<HandoverManifestResponse> getManifest(@PathVariable Long manifestId) {
        return ApiResponse.<HandoverManifestResponse>builder()
                .message(messageService.getMessage("success.handover_manifests.detail"))
                .result(handoverManifestService.getManifest(manifestId))
                .build();
    }
}
