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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryManifestRequest {
    @JsonProperty("post_office_code")
    private String postOfficeCode;

    @JsonProperty("courier_id")
    private Long courierId;

    @JsonProperty("vehicle_id")
    private String vehicleId;

    @JsonProperty("planned_date")
    private LocalDate plannedDate;

    @JsonProperty("planned_departure_at")
    private LocalDateTime plannedDepartureAt;

    @JsonProperty("order_codes")
    private List<String> orderCodes;

    @JsonProperty("note")
    private String note;
}
