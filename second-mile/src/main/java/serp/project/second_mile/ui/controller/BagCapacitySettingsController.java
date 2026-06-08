/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.request.UpdateBagCapacitySettingsRequest;
import serp.project.second_mile.dto.response.BagCapacitySettingsResponse;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.BagCapacitySettingsService;

@RestController
@RequestMapping("/api/v1/bags/capacity-settings")
@RequiredArgsConstructor
public class BagCapacitySettingsController {
    private final BagCapacitySettingsService bagCapacitySettingsService;
    private final MessageService messageService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<BagCapacitySettingsResponse> getSettings() {
        return ApiResponse.<BagCapacitySettingsResponse>builder()
                .message(messageService.getMessage("success.bag_capacity_settings.detail"))
                .result(bagCapacitySettingsService.getCurrentSettings())
                .build();
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<BagCapacitySettingsResponse> updateSettings(
            @Valid @RequestBody UpdateBagCapacitySettingsRequest request
    ) {
        return ApiResponse.<BagCapacitySettingsResponse>builder()
                .message(messageService.getMessage("success.bag_capacity_settings.update"))
                .result(bagCapacitySettingsService.updateCurrentSettings(request))
                .build();
    }
}
