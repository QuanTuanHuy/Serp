/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PostOfficeStaffResponse(
        Long id,
        String code,
        String fullName,
        String phoneNumber,
        String email,
        String avatarUrl,
        PostOfficeStaffRole role,
        PostOfficeStaffStatus status,
        LocalDate hireDate,
        Integer maxDailyStops,
        Integer maxDailyParcels,
        String notes,
        Long userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
}