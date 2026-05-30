/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import serp.project.first_mile.enums.TripStatus;

public record PickupTripLifecycleResponse(
        Long tripId,
        String tripCode,
        TripStatus tripStatus,
        Integer totalOrders,
        Integer checkedInOrders,
        Integer pendingCheckinOrders,
        Integer returnedToPostOfficeOrders,
        Boolean allOrdersCheckedIn
) {
}
