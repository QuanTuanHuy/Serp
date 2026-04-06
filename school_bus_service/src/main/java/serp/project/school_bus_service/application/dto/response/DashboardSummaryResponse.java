package serp.project.school_bus_service.application.dto.response;

public record DashboardSummaryResponse(
        long schoolCount,
        long parentCount,
        long studentCount,
        long busCount,
        long pendingRequestCount,
        long assignedRouteCount,
        long inProgressRouteCount,
        long completedTripCount) {
}
