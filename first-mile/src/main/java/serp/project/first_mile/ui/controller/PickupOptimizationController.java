/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.request.AutoAssignPickupPlanRequest;
import serp.project.first_mile.dto.request.ManualAssignPickupOrdersRequest;
import serp.project.first_mile.dto.request.OptimizePickupPlanRequest;
import serp.project.first_mile.dto.response.PickupAssignmentResponse;
import serp.project.first_mile.dto.response.PickupOptimizationResponse;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.PickupOptimizationService;

@RestController
@RequestMapping("/api/v1/pickup-optimization")
@RequiredArgsConstructor
public class PickupOptimizationController {

    private final PickupOptimizationService pickupOptimizationService;
    private final MessageService messageService;

    @PostMapping("/plan")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PickupOptimizationResponse> optimizePickupPlan(
            @Valid @RequestBody OptimizePickupPlanRequest request
    ) {
        return ApiResponse.<PickupOptimizationResponse>builder()
                .message(messageService.getMessage("success.pickup_optimization.plan"))
                .result(pickupOptimizationService.optimizePickupPlan(request))
                .build();
    }

    @PostMapping("/auto-assign")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PickupAssignmentResponse> autoAssignPickupPlan(
            @Valid @RequestBody AutoAssignPickupPlanRequest request
    ) {
        return ApiResponse.<PickupAssignmentResponse>builder()
                .message(messageService.getMessage("success.pickup_optimization.auto_assign"))
                .result(pickupOptimizationService.autoAssignPickupPlan(request))
                .build();
    }

    @PostMapping("/manual-assign")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PickupAssignmentResponse> manualAssignPickupOrders(
            @Valid @RequestBody ManualAssignPickupOrdersRequest request,
            @RequestParam(value = "force_assign", required = false) Boolean forceAssign
    ) {
        if (Boolean.TRUE.equals(forceAssign)) {
            request.setForceAssign(true);
        }

        return ApiResponse.<PickupAssignmentResponse>builder()
                .message(messageService.getMessage("success.pickup_optimization.manual_assign"))
                .result(pickupOptimizationService.manualAssignPickupOrders(request))
                .build();
    }
}
