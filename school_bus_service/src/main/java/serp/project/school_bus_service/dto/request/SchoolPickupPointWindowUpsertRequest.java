package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class SchoolPickupPointWindowUpsertRequest {

    @NotNull
    private Long schoolPickupPointId;

    @NotNull
    private Long schoolScheduleId;

    @NotNull
    private String direction;

    @NotNull
    private LocalTime windowStart;

    @NotNull
    private LocalTime windowEnd;

    private Double estimatedDistanceToSchoolKm;
    private Integer estimatedDurationToSchoolMin;
}
