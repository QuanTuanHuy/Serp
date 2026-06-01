/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

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
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreatePostOfficeHandoverManifestRequest;
import serp.project.first_mile.dto.request.DispatchPostOfficeHandoverManifestRequest;
import serp.project.first_mile.dto.request.ScanOutHandoverOrderRequest;
import serp.project.first_mile.dto.response.PostOfficeHandoverManifestResponse;
import serp.project.first_mile.enums.HandoverManifestStatus;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.PostOfficeHandoverManifestService;

@RestController
@RequestMapping("/api/v1/post-office-handover-manifests")
@RequiredArgsConstructor
public class PostOfficeHandoverManifestController {
    private final PostOfficeHandoverManifestService postOfficeHandoverManifestService;
    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PageResponse<PostOfficeHandoverManifestResponse>> listManifests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(name = "post_office_id", required = false) Long postOfficeId,
            @RequestParam(name = "target_hub_id", required = false) Long targetHubId,
            @RequestParam(required = false) HandoverManifestStatus status
    ) {
        return ApiResponse.<PageResponse<PostOfficeHandoverManifestResponse>>builder()
                .message(messageService.getMessage("success.post_office_handover_manifests.list"))
                .result(postOfficeHandoverManifestService.listManifests(page, size, postOfficeId, targetHubId, status))
                .build();
    }

    @GetMapping("/{manifestId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeHandoverManifestResponse> getManifest(@PathVariable Long manifestId) {
        return ApiResponse.<PostOfficeHandoverManifestResponse>builder()
                .message(messageService.getMessage("success.post_office_handover_manifests.detail"))
                .result(postOfficeHandoverManifestService.getManifest(manifestId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeHandoverManifestResponse> createManifest(
            @Valid @RequestBody CreatePostOfficeHandoverManifestRequest request
    ) {
        return ApiResponse.<PostOfficeHandoverManifestResponse>builder()
                .message(messageService.getMessage("success.post_office_handover_manifests.create"))
                .result(postOfficeHandoverManifestService.createManifest(request))
                .build();
    }

    @PostMapping("/{manifestId}/scan-out")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeHandoverManifestResponse> scanOrderOut(
            @PathVariable Long manifestId,
            @Valid @RequestBody ScanOutHandoverOrderRequest request
    ) {
        return ApiResponse.<PostOfficeHandoverManifestResponse>builder()
                .message(messageService.getMessage("success.post_office_handover_manifests.scan_out"))
                .result(postOfficeHandoverManifestService.scanOrderOut(manifestId, request))
                .build();
    }

    @PostMapping("/{manifestId}/dispatch")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeHandoverManifestResponse> dispatchManifest(
            @PathVariable Long manifestId,
            @RequestBody(required = false) DispatchPostOfficeHandoverManifestRequest request
    ) {
        return ApiResponse.<PostOfficeHandoverManifestResponse>builder()
                .message(messageService.getMessage("success.post_office_handover_manifests.dispatch"))
                .result(postOfficeHandoverManifestService.dispatchManifest(manifestId, request))
                .build();
    }

    @PostMapping("/{manifestId}/cancel")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeHandoverManifestResponse> cancelManifest(@PathVariable Long manifestId) {
        return ApiResponse.<PostOfficeHandoverManifestResponse>builder()
                .message(messageService.getMessage("success.post_office_handover_manifests.cancel"))
                .result(postOfficeHandoverManifestService.cancelManifest(manifestId))
                .build();
    }
}
