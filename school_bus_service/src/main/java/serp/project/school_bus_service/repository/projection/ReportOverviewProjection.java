package serp.project.school_bus_service.repository.projection;

public interface ReportOverviewProjection {
    Long getTotalRequests();

    Long getApprovedRequests();

    Long getCompletedTrips();

    Long getAttendanceEvents();

    Long getTripCount();

    Long getAttendanceCount();

    Long getCapacityCount();
}
