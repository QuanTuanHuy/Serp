/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.BagDestinationType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBagDistributionManifestRequest {
    @JsonProperty("origin_hub_id")
    @NotNull
    private Long originHubId;

    @JsonProperty("destination_type")
    @NotNull
    private BagDestinationType destinationType;

    @JsonProperty("destination_hub_id")
    private Long destinationHubId;

    @JsonProperty("destination_post_office_code")
    private String destinationPostOfficeCode;

    @JsonProperty("route_id")
    @NotNull
    private Long routeId;

    @JsonProperty("vehicle_id")
    @NotNull
    private Long vehicleId;

    @JsonProperty("planned_departure_at")
    @NotNull
    private LocalDateTime plannedDepartureAt;

    @JsonProperty("planned_arrival_at")
    @NotNull
    private LocalDateTime plannedArrivalAt;

    @JsonProperty("bag_ids")
    @NotEmpty
    private List<@NotNull Long> bagIds;

    private String note;
}
