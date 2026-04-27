/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PostOfficeStaffRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record PostOfficeStaffAssignmentResponse(
        Long id,
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        Long staffId,
        String staffCode,
        String staffFullName,
        PostOfficeStaffRole staffRole,
        LocalDate assignedFrom,
        LocalDate assignedTo,
        LocalTime shiftStartTime,
        LocalTime shiftEndTime,
        Boolean isPrimary,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
}