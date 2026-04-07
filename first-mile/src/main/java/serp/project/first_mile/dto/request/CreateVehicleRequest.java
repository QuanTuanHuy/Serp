/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.VehicleStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateVehicleRequest {

    @JsonProperty("license_plate")
    @NotBlank
    private String licensePlate;

    @JsonProperty("max_weight")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double maxWeight;

    @JsonProperty("max_volume")
    @DecimalMin(value = "0.0", inclusive = false)
    private Double maxVolume;

    @JsonProperty("post_office_id")
    private Long postOfficeId;

    @JsonProperty("post_office_staff_id")
    private Long postOfficeStaffId;

    @JsonProperty("status")
    @NotNull
    private VehicleStatus status;
}
