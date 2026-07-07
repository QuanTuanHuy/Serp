package serp.project.school_bus_service.repository.projection;

public interface RouteReadinessCountProjection {
    Long getReadyCount();

    Long getMissingBusCount();

    Long getMissingDriverCount();

    Long getMissingAttendantCount();
}
