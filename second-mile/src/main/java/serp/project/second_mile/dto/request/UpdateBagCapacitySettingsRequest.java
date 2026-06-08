/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBagCapacitySettingsRequest {
    @NotNull
    @Positive
    @JsonProperty("max_weight")
    private Double maxWeight;

    @NotNull
    @Positive
    @JsonProperty("max_volume")
    private Double maxVolume;

    @NotNull
    @Positive
    @JsonProperty("max_orders")
    private Integer maxOrders;
}
