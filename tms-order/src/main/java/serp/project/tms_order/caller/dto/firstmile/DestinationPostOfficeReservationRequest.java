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
public class DestinationPostOfficeReservationRequest {
    @JsonProperty("receiver_latitude")
    private Double receiverLatitude;

    @JsonProperty("receiver_longitude")
    private Double receiverLongitude;
}
