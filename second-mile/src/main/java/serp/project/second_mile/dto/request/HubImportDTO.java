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
import serp.project.second_mile.enums.HubStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubImportDTO {
    @JsonProperty("stt")
    private String stt;

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

    @JsonProperty("status")
    private HubStatus status;
}
