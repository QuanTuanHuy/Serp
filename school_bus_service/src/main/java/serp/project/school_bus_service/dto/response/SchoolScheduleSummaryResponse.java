package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SchoolScheduleSummaryResponse {
    private Long id;
    private String code;
    private String name;
    private String shift;
    private List<String> days;
    private LocalTime arrivalDeadline;
    private LocalTime departureTime;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isDefault;
    private Boolean isActive;
}
