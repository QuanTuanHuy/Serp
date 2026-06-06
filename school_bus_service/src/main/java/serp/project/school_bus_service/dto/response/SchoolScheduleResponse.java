package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class SchoolScheduleResponse extends BaseResponse {

    private Long schoolId;
    private String schoolName;
    private String scheduleCode;
    private String scheduleName;
    private String educationLevel;
    private String grade;
    private String shiftType;
    private List<String> daysOfWeek;
    private LocalTime arrivalDeadline;
    private LocalTime departureTime;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean isDefault;
}
