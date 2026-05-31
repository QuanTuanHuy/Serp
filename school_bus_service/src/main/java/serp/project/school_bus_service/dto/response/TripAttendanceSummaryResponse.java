package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TripAttendanceSummaryResponse {
    private int totalStudents;
    private int planned;
    private int boarded;
    private int droppedOff;
    private int absent;
    private int noShow;
    private int notServed;
}
