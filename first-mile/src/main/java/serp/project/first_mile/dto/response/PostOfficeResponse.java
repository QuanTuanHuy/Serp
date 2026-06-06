/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PostOfficeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record PostOfficeResponse(
        Long id,
        String code,
        String name,
        String provinceCode,
        String wardCode,
        String addressDetail,
        String phoneNumber,
        String imageUrl,
        LocalDate operationalStartDate,
        LocalDate operationalEndDate,
        LocalTime workingStartTime,
        LocalTime workingEndTime,
        Integer serviceRadiusM,
        Integer dailyCapacity,
        Integer currentLoad,
        Integer deliveryCapacity,
        Integer currentDeliveryLoad,
        Integer priority,
        Double latitude,
        Double longitude,
        PostOfficeStatus status,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId,
        Long hubId
) {
}
