/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.dto;

import java.time.LocalDateTime;

public record OrderTimelineContext(
        LocalDateTime eventTime,
        Long tripId,
        String tripCode,
        Long postOfficeId,
        String postOfficeCode,
        String postOfficeName,
        Long courierStaffId,
        String courierCode,
        String courierName,
        Long vehicleId,
        String vehicleLicensePlate,
        Double latitude,
        Double longitude,
        String locationLabel
) {
    public static OrderTimelineContext empty() {
        return new OrderTimelineContext(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
