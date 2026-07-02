package serp.project.school_bus_service.repository.projection;

public interface RouteStopStudentCountProjection {

    Long getRouteStopId();

    Integer getBoardingCount();

    Integer getDropoffCount();
}
