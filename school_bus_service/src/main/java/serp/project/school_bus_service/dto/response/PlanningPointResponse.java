package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
public class PlanningPointResponse {
    private Long pointId;
    private String pointCode;
    private String pointName;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private String pointRole; // PICKUP / DROPOFF
    private int studentCount;

    private LocalTime windowStart;
    private LocalTime windowEnd;
}
