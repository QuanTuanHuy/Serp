/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.second_mile.dto.ApiResponse;
import serp.project.second_mile.dto.response.HubStaffAssignmentResponse;
import serp.project.second_mile.dto.response.HubStaffResponse;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.exception.MessageService;
import serp.project.second_mile.service.HubStaffAssignmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hub-staff-assignments")
@RequiredArgsConstructor
public class HubStaffAssignmentController {
    private final HubStaffAssignmentService hubStaffAssignmentService;
    private final MessageService messageService;

    @PutMapping("/staffs/{staffId}/hubs/{hubId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<HubStaffAssignmentResponse> assignStaffToHub(
            @PathVariable Long staffId,
            @PathVariable Long hubId
    ) {
        return ApiResponse.<HubStaffAssignmentResponse>builder()
                .message(messageService.getMessage("success.hub_staff_assignments.assign"))
                .result(hubStaffAssignmentService.assignStaffToHub(staffId, hubId))
                .build();
    }

    @GetMapping("/hubs/{hubId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER', 'TMS_HUB_EMPLOYEE')")
    public ApiResponse<List<HubStaffAssignmentResponse>> listActiveAssignmentsByHub(
            @PathVariable Long hubId,
            @RequestParam(required = false) HubStaffRole role
    ) {
        return ApiResponse.<List<HubStaffAssignmentResponse>>builder()
                .message(messageService.getMessage("success.hub_staff_assignments.list"))
                .result(hubStaffAssignmentService.listActiveAssignmentsByHub(hubId, role))
                .build();
    }

    @GetMapping("/staffs")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<List<HubStaffResponse>> getAssignableStaffByRole(
            @RequestParam HubStaffRole role,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.<List<HubStaffResponse>>builder()
                .message(messageService.getMessage("success.hub_staff_assignments.assignable"))
                .result(hubStaffAssignmentService.getAssignableStaffByRole(role, keyword))
                .build();
    }

    @PutMapping("/{assignmentId}/unassign")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_HUB_MANAGER')")
    public ApiResponse<HubStaffAssignmentResponse> unassignStaffFromHub(@PathVariable Long assignmentId) {
        return ApiResponse.<HubStaffAssignmentResponse>builder()
                .message(messageService.getMessage("success.hub_staff_assignments.unassign"))
                .result(hubStaffAssignmentService.unassignStaffFromHub(assignmentId))
                .build();
    }
}
