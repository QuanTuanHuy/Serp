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
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.enums.VehicleType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleRequest {
    @JsonProperty("license_plate")
    @NotBlank
    @Size(max = 50)
    private String licensePlate;

    @JsonProperty("vehicle_type")
    @NotNull
    private VehicleType vehicleType;

    @JsonProperty("max_weight")
    @Min(0)
    private double maxWeight;

    @JsonProperty("max_volume")
    @Min(0)
    private double maxVolume;

    @JsonProperty("max_bags")
    @Min(0)
    private int maxBags;

    @JsonProperty("image_url")
    @Size(max = 2048)
    private String imageUrl;

    @JsonProperty("hub_id")
    @NotNull
    private Long hubId;

    @JsonProperty("assigned_staff_id")
    private Long assignedStaffId;

    @JsonProperty("status")
    @NotNull
    private VehicleStatus status;
}

