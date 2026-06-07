package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TripAttendanceManifestResponse {
    private Long tripId;
    private String tripCode;
    private Long routeId;
    private String routeCode;
    private String routeName;
    private String routeDirection;
    private String tripStatus;
    private String serviceDate;
    private String routeGeometry;
    private Double distanceKm;
    private Integer durationMin;
    private TripAttendanceSummaryResponse summary;
    private List<TripAttendanceStopItem> stops;
    private List<TripAttendanceStudentItem> students;

    @Getter
    @Setter
    public static class TripAttendanceStopItem {
        private Long routeStopId;
        private Integer stopOrder;
        /** Location type: DEPOT | SCHOOL | PICKUP_POINT */
        private String locationType;
        /** Stop purpose: START_TERMINAL | PICKUP | DROPOFF | END_TERMINAL */
        private String stopPurpose;
        /** Human-readable stop name. */
        private String displayName;
        private String stopStatus;
        private Double latitude;
        private Double longitude;
        private Integer plannedBoardingCount;
        private Integer plannedDropoffCount;
        private Integer actualBoardedCount;
        private Integer actualDroppedCount;
        private String plannedArrivalTime;
        private String plannedDepartureTime;
        private String actualArrivalTime;
        private String actualDepartureTime;
        private Integer studentCount;
    }

    @Getter
    @Setter
    public static class TripAttendanceStudentItem {
        private Long tripStudentId;
        private Long studentId;
        private String studentName;
        private String studentCode;
        private String status;
        private Long pickupStopId;
        private Long dropoffStopId;
        private Long subscriptionId;
        private String note;
    }
}

