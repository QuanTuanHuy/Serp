package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffAssignmentRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffRequest;
import serp.project.first_mile.dto.response.PostOfficeStaffAssignmentResponse;
import serp.project.first_mile.dto.response.PostOfficeStaffResponse;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.PostOfficeStaffService;

@RestController
@RequestMapping("/api/v1/post-office-staffs")
@RequiredArgsConstructor
public class PostOfficeStaffController {

    private final PostOfficeStaffService postOfficeStaffService;
    private final MessageService messageService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeStaffResponse> updatePostOfficeStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostOfficeStaffRequest request
    ) {
        return ApiResponse.<PostOfficeStaffResponse>builder()
                .message(messageService.getMessage("success.post_office_staffs.update"))
                .result(postOfficeStaffService.updatePostOfficeStaff(id, request))
                .build();
    }

    @PutMapping("/{id}/assignments/courier/post-offices/{postOfficeId}")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeStaffAssignmentResponse> assignCourierToPostOffice(
            @PathVariable Long id,
            @PathVariable Long postOfficeId
    ) {
        return ApiResponse.<PostOfficeStaffAssignmentResponse>builder()
                .message(messageService.getMessage("success.post_office_staffs.assign.courier"))
                .result(postOfficeStaffService.assignCourierToPostOffice(id, postOfficeId))
                .build();
    }

    @PutMapping("/{id}/assignments/manager/post-offices/{postOfficeId}")
    @PreAuthorize("hasRole('TMS_ADMIN')")
    public ApiResponse<PostOfficeStaffAssignmentResponse> assignManagerToPostOffice(
            @PathVariable Long id,
            @PathVariable Long postOfficeId
    ) {
        return ApiResponse.<PostOfficeStaffAssignmentResponse>builder()
                .message(messageService.getMessage("success.post_office_staffs.assign.manager"))
                .result(postOfficeStaffService.assignManagerToPostOffice(id, postOfficeId))
                .build();
    }

    @PutMapping("/assignments/{assignmentId}/courier-details")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER')")
    public ApiResponse<PostOfficeStaffAssignmentResponse> updateCourierAssignmentDetails(
            @PathVariable Long assignmentId,
            @RequestBody UpdatePostOfficeStaffAssignmentRequest request
    ) {
        return ApiResponse.<PostOfficeStaffAssignmentResponse>builder()
                .message(messageService.getMessage("success.post_office_staffs.assignments.update"))
                .result(postOfficeStaffService.updateCourierAssignmentDetails(assignmentId, request))
                .build();
    }

    @PostMapping("/{id}/avatar")
    @PreAuthorize("hasAnyRole('TMS_ADMIN', 'TMS_POSTOFFICER_MANAGER', 'TMS_POSTOFFICER')")
    public ApiResponse<PostOfficeStaffResponse> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.<PostOfficeStaffResponse>builder()
                .message(messageService.getMessage("success.post_office_staffs.avatar.upload"))
                .result(postOfficeStaffService.uploadAvatar(id, file))
                .build();
    }

}
