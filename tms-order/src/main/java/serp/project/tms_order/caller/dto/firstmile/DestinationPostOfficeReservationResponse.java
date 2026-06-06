/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.caller.dto.firstmile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationPostOfficeReservationResponse {
    private Long id;
    private String code;
    private String name;

    @JsonProperty("current_delivery_load")
    private Integer currentDeliveryLoad;

    @JsonProperty("delivery_capacity")
    private Integer deliveryCapacity;
}
