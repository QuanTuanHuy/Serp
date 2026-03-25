/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

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
import serp.project.first_mile.enums.PostOfficeStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostOfficeRequest {
        @JsonProperty("code")
        @NotBlank
        private String code;

        @JsonProperty("name")
        @NotBlank
        private String name;

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

        @JsonProperty("operational_start_date")
        private LocalDate operationalStartDate;

        @JsonProperty("operational_end_date")
        private LocalDate operationalEndDate;

        @JsonProperty("working_start_time")
        private LocalTime workingStartTime;

        @JsonProperty("working_end_time")
        private LocalTime workingEndTime;

        @JsonProperty("service_radius_m")
        @NotNull
        @Min(1)
        private Integer serviceRadiusM;

        @JsonProperty("daily_capacity")
        @Min(0)
        private Integer dailyCapacity;

        @JsonProperty("current_load")
        @Min(0)
        private Integer currentLoad;

        @JsonProperty("priority")
        @Min(0)
        private Integer priority;

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
        private PostOfficeStatus status;

        @JsonProperty("tenant_id")
        private Long tenantId;
}
