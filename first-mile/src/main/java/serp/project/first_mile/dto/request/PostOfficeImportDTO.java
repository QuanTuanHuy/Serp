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
import serp.project.first_mile.enums.PostOfficeStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostOfficeImportDTO {
    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("province_code")
    private String provinceCode;

    @JsonProperty("ward_code")
    private String wardCode;

    @JsonProperty("address_detail")
    private String addressDetail;

    @JsonProperty("phone_number")
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
    private Integer serviceRadiusM;

    @JsonProperty("status")
    private PostOfficeStatus status;

    @JsonProperty("source_rows")
    @Builder.Default
    private List<Integer> sourceRows = new ArrayList<>();
}
