/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.enums.VehicleType;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String licensePlate,
        Double maxWeight,
        Double maxVolume,
        String imageUrl,
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        Long postOfficeStaffId,
        VehicleStatus status,
        VehicleType vehicleType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
}
