package serp.project.logistics2.dto.request;

import lombok.Data;

@Data
public class VehicleUpdateForm {

    private String licensePlate;

    private String vehicleType;

    private Long maxWeightKg;

    private Double maxVolumeCbm;

}
