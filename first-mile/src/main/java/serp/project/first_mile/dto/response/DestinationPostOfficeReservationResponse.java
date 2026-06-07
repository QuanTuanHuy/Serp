/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DestinationPostOfficeReservationResponse(
        Long id,
        String code,
        String name,
        @JsonProperty("current_delivery_load") Integer currentDeliveryLoad,
        @JsonProperty("delivery_capacity") Integer deliveryCapacity
) {
}
