/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRouteRequest {
    @JsonProperty("route_code")
    @Size(max = 100)
    private String routeCode;

    @JsonProperty("route_name")
    @NotBlank
    @Size(max = 255)
    private String routeName;

    @JsonProperty("origin_type")
    private RouteEndpointType originType;

    @JsonProperty("origin_hub_id")
    private Long originHubId;

    @JsonProperty("origin_post_office_code")
    @Size(max = 255)
    private String originPostOfficeCode;

    @JsonProperty("destination_type")
    @NotNull
    private RouteDestinationType destinationType;

    @JsonProperty("destination_hub_id")
    private Long destinationHubId;

    @JsonProperty("destination_post_office_code")
    @Size(max = 255)
    private String destinationPostOfficeCode;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("estimated_distance_km")
    @Min(0)
    private Double estimatedDistanceKm;

    @JsonProperty("estimated_duration_minutes")
    @Min(0)
    private Integer estimatedDurationMinutes;

    @JsonProperty("fixed_departure_time")
    private LocalTime fixedDepartureTime;

    @NotNull
    private RouteStatus status;

    private String note;
}
