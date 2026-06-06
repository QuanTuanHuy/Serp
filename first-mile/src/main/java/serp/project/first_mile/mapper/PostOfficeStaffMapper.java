/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.mapper;

import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.dto.response.PostOfficeStaffAssignmentResponse;
import serp.project.first_mile.dto.response.PostOfficeStaffResponse;
import serp.project.first_mile.enums.PostOfficeStaffRole;

public final class PostOfficeStaffMapper {

    private PostOfficeStaffMapper() {
    }

    public static PostOfficeStaffResponse toResponse(PostOfficeStaff staff) {
        return new PostOfficeStaffResponse(
                staff.getId(),
                staff.getCode(),
                staff.getFullName(),
                staff.getPhoneNumber(),
                staff.getEmail(),
                staff.getAvatarUrl(),
                staff.getRole(),
                staff.getStatus(),
                staff.getHireDate(),
                staff.getMaxDailyStops(),
                staff.getMaxDailyParcels(),
                staff.getNotes(),
                staff.getUserId(),
                staff.getCreatedAt(),
                staff.getUpdatedAt(),
                staff.getCreatedBy(),
                staff.getUpdatedBy(),
                staff.getTenantId()
        );
    }

    public static PostOfficeStaffAssignmentResponse toAssignmentResponse(PostOfficeStaffAssignment assignment) {
        Long postOfficeId = null;
        String postOfficeCode = null;
        String postOfficeName = null;
        if (assignment.getPostOffice() != null) {
            postOfficeId = assignment.getPostOffice().getId();
            postOfficeCode = assignment.getPostOffice().getCode();
            postOfficeName = assignment.getPostOffice().getName();
        }

        Long staffId = null;
        String staffCode = null;
        String staffFullName = null;
        PostOfficeStaffRole staffRole = null;
        if (assignment.getStaff() != null) {
            staffId = assignment.getStaff().getId();
            staffCode = assignment.getStaff().getCode();
            staffFullName = assignment.getStaff().getFullName();
            staffRole = assignment.getStaff().getRole();
        }

        return new PostOfficeStaffAssignmentResponse(
                assignment.getId(),
                postOfficeId,
                postOfficeCode,
                postOfficeName,
                staffId,
                staffCode,
                staffFullName,
                staffRole,
                assignment.getAssignedFrom(),
                assignment.getAssignedTo(),
                assignment.getShiftStartTime(),
                assignment.getShiftEndTime(),
                assignment.getIsPrimary(),
                assignment.getNotes(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt(),
                assignment.getCreatedBy(),
                assignment.getUpdatedBy(),
                assignment.getTenantId()
        );
    }
}