package serp.project.school_bus_service.repository.projection;

public interface TripAttendanceStudentProjection {

    Long getTripStudentId();

    Long getStudentId();

    String getStudentName();

    String getStudentCode();

    String getStatus();

    Long getPickupStopId();

    Long getDropoffStopId();

    Long getSubscriptionId();

    String getNote();
}
