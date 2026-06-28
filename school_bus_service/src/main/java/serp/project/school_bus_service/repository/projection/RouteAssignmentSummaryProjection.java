package serp.project.school_bus_service.repository.projection;

public interface RouteAssignmentSummaryProjection {

    Long getRouteId();

    Long getBusId();

    String getBusPlateNumber();

    Integer getBusCapacity();

    String getBusStatus();

    Long getDriverId();

    String getDriverName();

    Long getAttendantId();

    String getAttendantName();
}
