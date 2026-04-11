/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.PickupShift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualAssignPickupOrdersRequest {

    @JsonProperty("post_office_id")
    @NotNull
    private Long postOfficeId;

    @JsonProperty("courier_staff_id")
    @NotNull
    private Long courierStaffId;

    @JsonProperty("order_ids")
    @NotEmpty
    private List<Long> orderIds;

    @JsonProperty("shift")
    @NotNull
    private PickupShift shift;

    @JsonProperty("trip_date")
    private LocalDate tripDate;

    @JsonProperty("planning_start_time")
    private LocalDateTime planningStartTime;

    @JsonProperty("planning_end_time")
    private LocalDateTime planningEndTime;

    @JsonProperty("vehicle")
    @Pattern(regexp = "(?i)car|bike|taxi|truck|hd")
    private String vehicle;

    @JsonProperty("average_speed_kmph")
    @DecimalMin(value = "1.0")
    private Double averageSpeedKmph;

    @JsonProperty("service_minutes_per_stop")
    @Min(1)
    private Integer serviceMinutesPerStop;

    @JsonProperty("allow_lateness")
    private Boolean allowLateness;

    @JsonProperty("enforce_planning_end")
    private Boolean enforcePlanningEnd;

    @JsonProperty("enforce_capacity")
    private Boolean enforceCapacity;
}
