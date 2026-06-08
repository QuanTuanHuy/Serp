/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.BagDestinationType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoPlanBagDistributionRequest {
    @JsonProperty("origin_hub_id")
    @NotNull
    private Long originHubId;

    @JsonProperty("destination_type")
    private BagDestinationType destinationType;

    @JsonProperty("destination_hub_id")
    private Long destinationHubId;

    @JsonProperty("destination_post_office_code")
    private String destinationPostOfficeCode;

    @JsonProperty("planned_departure_at")
    @NotNull
    private LocalDateTime plannedDepartureAt;

    @JsonProperty("planned_arrival_at")
    @NotNull
    private LocalDateTime plannedArrivalAt;

    @JsonProperty("sealed_sla_hours")
    private Integer sealedSlaHours;

    @JsonProperty("execute")
    private Boolean execute;

    private String note;
}
