/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHandoverManifestRequest {
    @JsonProperty("origin_post_office_code")
    @NotBlank
    private String originPostOfficeCode;

    @JsonProperty("target_hub_id")
    @NotNull
    private Long targetHubId;

    @JsonProperty("vehicle_id")
    @NotNull
    private Long vehicleId;

    @JsonProperty("route_id")
    @NotNull
    private Long routeId;

    @JsonProperty("planned_departure_at")
    @NotNull
    private LocalDateTime plannedDepartureAt;

    @JsonProperty("planned_arrival_at")
    @NotNull
    private LocalDateTime plannedArrivalAt;

    @JsonProperty("origin_post_office_latitude")
    private Double originPostOfficeLatitude;

    @JsonProperty("origin_post_office_longitude")
    private Double originPostOfficeLongitude;

    @JsonProperty("order_codes")
    @NotEmpty
    private List<@NotBlank String> orderCodes;
}
