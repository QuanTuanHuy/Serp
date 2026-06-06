/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.response;

public record OrderDropOffPostOfficeSuggestionResponse(
        Long id,
        String code,
        String name,
        String provinceCode,
        String wardCode,
        String addressDetail,
        Integer priority,
        Integer currentLoad,
        Integer dailyCapacity,
        Integer remainingCapacity,
        Double latitude,
        Double longitude,
        Double distanceMeters
) {
}
