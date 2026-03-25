/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.PostOfficeStatus;

import java.time.LocalDateTime;

public record PostOfficeResponse(
        Long id,
        String code,
        String name,
        String provinceCode,
        String wardCode,
        String addressDetail,
        String phoneNumber,
        Integer serviceRadiusM,
        Integer dailyCapacity,
        Integer currentLoad,
        Integer priority,
        Double latitude,
        Double longitude,
        PostOfficeStatus status,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long tenantId
) {
}
