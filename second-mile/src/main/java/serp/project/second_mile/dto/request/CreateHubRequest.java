/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.HubStatus;
import serp.project.second_mile.enums.HubType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateHubRequest {
    @JsonProperty("code")
    @NotBlank
    private String code;

    @JsonProperty("name")
    @NotBlank
    private String name;

    @JsonProperty("hub_type")
    @NotNull
    private HubType hubType;

    @JsonProperty("province_code")
    @NotBlank
    private String provinceCode;

    @JsonProperty("ward_code")
    @NotBlank
    private String wardCode;

    @JsonProperty("address_detail")
    @NotBlank
    private String addressDetail;

    @JsonProperty("phone_number")
    @Size(max = 15)
    private String phoneNumber;

    @JsonProperty("working_start_time")
    private LocalDateTime workingStartTime;

    @JsonProperty("working_end_time")
    private LocalDateTime workingEndTime;

    @JsonProperty("daily_capacity")
    @Min(0)
    private Integer dailyCapacity;

    @JsonProperty("current_load")
    @Min(0)
    private Integer currentLoad;

    @JsonProperty("latitude")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @JsonProperty("longitude")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @JsonProperty("status")
    @NotNull
    private HubStatus status;
}
