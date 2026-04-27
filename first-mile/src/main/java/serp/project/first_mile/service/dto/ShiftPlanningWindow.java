package serp.project.first_mile.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ShiftPlanningWindow(
        LocalDate tripDate,
        LocalDateTime planningStartTime,
        LocalDateTime planningEndTime
) {
}