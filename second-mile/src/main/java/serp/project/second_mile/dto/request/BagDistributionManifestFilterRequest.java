/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagDistributionManifestStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BagDistributionManifestFilterRequest {
    @JsonProperty("origin_hub_id")
    private Long originHubId;

    @JsonProperty("destination_type")
    private BagDestinationType destinationType;

    @JsonProperty("destination_hub_id")
    private Long destinationHubId;

    @JsonProperty("destination_post_office_code")
    private String destinationPostOfficeCode;

    @JsonProperty("route_id")
    private Long routeId;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("assigned_driver_id")
    private Long assignedDriverId;

    private BagDistributionManifestStatus status;
}
