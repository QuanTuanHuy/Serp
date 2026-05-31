package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

/** A single student eligible for route planning. */
@Getter
@Setter
public class EligibleStudentResponse {

    private Long studentId;
    private String studentName;
    private String studentCode;

    private Long subscriptionId;
    private String subscriptionCode;
    private String tripOption;

    private Long pickupPointId;
    private String pickupPointName;
    private Double pickupPointLatitude;
    private Double pickupPointLongitude;

    private Long dropoffPointId;
    private String dropoffPointName;
    private Double dropoffPointLatitude;
    private Double dropoffPointLongitude;

    /** The relevant point for this direction (pickup for OUTBOUND, dropoff for RETURN) */
    private Long relevantPointId;
    private String relevantPointName;
    private Double relevantPointLatitude;
    private Double relevantPointLongitude;

    /** Time window at the relevant point (from SchoolPickupPointWindow) */
    private LocalTime windowStart;
    private LocalTime windowEnd;

    private String specialNote;
}
