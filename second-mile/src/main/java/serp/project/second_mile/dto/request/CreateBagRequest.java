/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.BagStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBagRequest {
    @JsonProperty("bag_code")
    @NotBlank
    @Size(max = 100)
    private String bagCode;

    @JsonProperty("origin_hub_id")
    @NotNull
    private Long originHubId;

    @JsonProperty("destination_type")
    @NotNull
    private BagDestinationType destinationType;

    @JsonProperty("destination_hub_id")
    private Long destinationHubId;

    @JsonProperty("destination_post_office_code")
    @Size(max = 255)
    private String destinationPostOfficeCode;

    @JsonProperty("vehicle_id")
    private Long vehicleId;

    @JsonProperty("max_weight")
    private Double maxWeight;

    @JsonProperty("max_volume")
    private Double maxVolume;

    @JsonProperty("max_orders")
    private Integer maxOrders;

    @JsonProperty("status")
    private BagStatus status;

    @JsonProperty("note")
    private String note;
}
