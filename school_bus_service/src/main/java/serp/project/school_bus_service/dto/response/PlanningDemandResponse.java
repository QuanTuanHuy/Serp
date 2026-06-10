package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
public class PlanningDemandResponse {
    private Long subscriptionId;
    private String subscriptionCode;

    private Long studentId;
    private String studentCode;
    private String studentName;

    private Long schoolId;
    private String schoolName;


    private String tripOption;
    private String tripOptionLabel;

    private Long pointId;
    private String pointCode;
    private String pointName;
    private BigDecimal latitude;
    private BigDecimal longitude;

    private LocalTime windowStart;
    private LocalTime windowEnd;
}
