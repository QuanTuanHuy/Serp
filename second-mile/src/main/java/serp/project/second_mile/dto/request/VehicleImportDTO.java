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
import serp.project.second_mile.enums.VehicleStatus;
import serp.project.second_mile.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImportDTO {

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("max_bags")
    private Integer maxBags;

    @JsonProperty("max_weight")
    private Double maxWeight;

    @JsonProperty("max_volume")
    private Double maxVolume;

    @JsonProperty("hub_id")
    private Long hubId;

    @JsonProperty("hub_code")
    private String hubCode;

    @JsonProperty("hub_name")
    private String hubName;

    @JsonProperty("assigned_staff_id")
    private Long assignedStaffId;

    @JsonProperty("driver_code")
    private String driverCode;

    @JsonProperty("driver_name")
    private String driverName;

    @JsonProperty("vehicle_type")
    private VehicleType vehicleType;

    @JsonProperty("status")
    private VehicleStatus status;

    @JsonProperty("source_rows")
    @Builder.Default
    private List<Integer> sourceRows = new ArrayList<>();
}
