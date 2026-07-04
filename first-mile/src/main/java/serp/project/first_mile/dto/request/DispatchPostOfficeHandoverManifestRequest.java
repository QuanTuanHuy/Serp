/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchPostOfficeHandoverManifestRequest {
    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("route_id")
    private Long routeId;

    @JsonProperty("planned_departure_at")
    private LocalDateTime plannedDepartureAt;

    @JsonProperty("planned_arrival_at")
    private LocalDateTime plannedArrivalAt;

    @JsonProperty("seal_code")
    private String sealCode;

    private String note;
}
