/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

public record OriginPostOfficeReservationResponse(
        Long id,
        String code,
        String name,
        Integer currentLoad,
        Integer dailyCapacity
) {
}
