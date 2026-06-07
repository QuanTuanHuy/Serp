/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.enums.VehicleType;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String licensePlate,
        VehicleType vehicleType,
        double maxWeight,
        double maxVolume,
        int maxBags,
        String imageUrl,
        Long hubId,
        Long assignedStaffId,
        VehicleStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
}

