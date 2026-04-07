/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.VehicleStatus;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String licensePlate,
        Double maxWeight,
        Double maxVolume,
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        Long postOfficeStaffId,
        VehicleStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
}
