package serp.project.school_bus_service.repository.projection;

public interface RouteDispatchSummaryProjection {
    Long getTotalRoutes();

    Long getPlannedRoutes();

    Long getTripCreatedRoutes();
}
