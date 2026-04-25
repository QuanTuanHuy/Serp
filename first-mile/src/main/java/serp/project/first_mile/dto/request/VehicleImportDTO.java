/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.enums.VehicleType;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleImportDTO {

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("max_weight")
    private Double maxWeight;

    @JsonProperty("max_volume")
    private Double maxVolume;

    @JsonProperty("post_office_id")
    private Long postOfficeId;

    @JsonProperty("post_office_code")
    private String postOfficeCode;

    @JsonProperty("post_office_name")
    private String postOfficeName;

    @JsonProperty("post_office_staff_id")
    private Long postOfficeStaffId;

    @JsonProperty("post_office_staff_code")
    private String postOfficeStaffCode;

    @JsonProperty("post_office_staff_name")
    private String postOfficeStaffName;

    @JsonProperty("vehicle_type")
    private VehicleType vehicleType;

    @JsonProperty("status")
    private VehicleStatus status;

    @JsonProperty("source_rows")
    @Builder.Default
    private List<Integer> sourceRows = new ArrayList<>();
}