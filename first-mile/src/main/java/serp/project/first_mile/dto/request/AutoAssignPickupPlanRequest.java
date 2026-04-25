/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PickupOptimizationEffort;
import serp.project.first_mile.enums.PickupOptimizationGoal;
import serp.project.first_mile.enums.PickupShift;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoAssignPickupPlanRequest {

    @JsonProperty("post_office_id")
    @NotNull
    private Long postOfficeId;

    @JsonProperty("shift")
    @NotNull
    private PickupShift shift;

    @JsonProperty("trip_date")
    private LocalDate tripDate;

    @JsonProperty("planning_start_time")
    private LocalDateTime planningStartTime;

    @JsonProperty("planning_end_time")
    private LocalDateTime planningEndTime;

    @JsonProperty("courier_ids")
    private List<Long> courierIds;

    @JsonProperty("candidate_statuses")
    private List<OrderStatus> candidateStatuses;

    @JsonProperty("vehicle")
    @Pattern(regexp = "(?i)car|bike|taxi|truck|hd")
    private String vehicle;

    @JsonProperty("order_limit")
    @Min(1)
    private Integer orderLimit;

    @JsonProperty("optimization_goal")
    private PickupOptimizationGoal optimizationGoal;

    @JsonProperty("optimization_effort")
    private PickupOptimizationEffort optimizationEffort;

    @JsonProperty("average_speed_kmph")
    @DecimalMin(value = "1.0")
    private Double averageSpeedKmph;

    @JsonProperty("service_minutes_per_stop")
    @Min(1)
    private Integer serviceMinutesPerStop;

    @JsonProperty("max_iterations")
    @Min(1)
    private Integer maxIterations;

    @JsonProperty("max_runtime_millis")
    @Min(100)
    private Long maxRuntimeMillis;

    @JsonProperty("destroy_rate")
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "0.90")
    private Double destroyRate;

    @JsonProperty("initial_temperature")
    @DecimalMin(value = "0.0001")
    private Double initialTemperature;

    @JsonProperty("cooling_rate")
    @DecimalMin(value = "0.80")
    @DecimalMax(value = "0.9999")
    private Double coolingRate;

    @JsonProperty("allow_lateness")
    private Boolean allowLateness;

    @JsonProperty("enforce_planning_end")
    private Boolean enforcePlanningEnd;

    @JsonProperty("enforce_capacity")
    private Boolean enforceCapacity;

    @JsonProperty("distance_weight")
    @DecimalMin(value = "0.0")
    private Double distanceWeight;

    @JsonProperty("lateness_weight")
    @DecimalMin(value = "0.0")
    private Double latenessWeight;

    @JsonProperty("unassigned_penalty")
    @DecimalMin(value = "0.0")
    private Double unassignedPenalty;

    @JsonProperty("used_route_penalty")
    @DecimalMin(value = "0.0")
    private Double usedRoutePenalty;
}
