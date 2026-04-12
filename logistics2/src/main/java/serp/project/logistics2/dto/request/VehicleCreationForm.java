package serp.project.logistics2.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.logistics2.constant.VehicleType;
import serp.project.logistics2.validator.EnumValidator;

@Data
public class VehicleCreationForm {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @EnumValidator(enumClass = VehicleType.class, message = "Invalid vehicle type")
    private String vehicleType;

    @NotNull(message = "Max weight (kg) is required")
    @Min(value = 1, message = "Max weight (kg) must be greater than 0")
    private Long maxWeightKg;

    @NotNull(message = "Max volume (cbm) is required")
    private Double maxVolumeCbm;

}
