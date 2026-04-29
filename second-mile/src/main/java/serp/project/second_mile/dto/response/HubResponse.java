/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.response;

import serp.project.second_mile.enums.HubStatus;
import serp.project.second_mile.enums.HubType;

import java.time.LocalDateTime;

public record HubResponse(
        Long id,
        String code,
        String name,
        HubType hubType,
        String provinceCode,
        String wardCode,
        String addressDetail,
        String phoneNumber,
        String imageUrl,
        LocalDateTime workingStartTime,
        LocalDateTime workingEndTime,
        Integer dailyCapacity,
        Integer currentLoad,
        Double latitude,
        Double longitude,
        HubStatus status,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy,
        Long tenantId
) {
}
