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
public class OriginPostOfficeReservationRequest {
    @JsonProperty("sender_latitude")
    private Double senderLatitude;

    @JsonProperty("sender_longitude")
    private Double senderLongitude;
}
