package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class SchoolScheduleUpsertRequest {

    @NotBlank
    private String scheduleName;

    private String educationLevel;
    private String grade;

    @NotBlank
    private String shiftType;

    private List<String> daysOfWeek;
    private LocalTime arrivalDeadline;
    private LocalTime departureTime;

    @NotNull
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    private Boolean isDefault;
    private Boolean isActive;
}
