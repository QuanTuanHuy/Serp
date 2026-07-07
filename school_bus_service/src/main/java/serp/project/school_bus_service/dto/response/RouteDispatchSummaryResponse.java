package serp.project.school_bus_service.dto.response;

public record RouteDispatchSummaryResponse(
        long totalRoutes,
        long plannedRoutes,
        long tripCreatedRoutes) {
}
