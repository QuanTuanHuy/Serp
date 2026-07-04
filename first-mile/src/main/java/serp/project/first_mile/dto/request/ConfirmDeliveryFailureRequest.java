/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDeliveryFailureRequest {
    @JsonProperty("failure_reason")
    private String failureReason;

    @JsonProperty("note")
    private String note;

    @JsonProperty("current_lat")
    private Double currentLat;

    @JsonProperty("current_lng")
    private Double currentLng;
}
