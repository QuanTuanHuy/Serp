package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class SchoolPickupPointWindowResponse extends BaseResponse {

    private Long schoolPickupPointId;
    private Long schoolScheduleId;
    private String scheduleName;
    private String direction;
    private LocalTime windowStart;
    private LocalTime windowEnd;
    private Double estimatedDistanceToSchoolKm;
    private Integer estimatedDurationToSchoolMin;
}
