package serp.project.school_bus_service.application.dto.response;

public record OperationalReportResponse(
        long totalRequests,
        long approvedRequests,
        long rejectedRequests,
        long activeRoutes,
        long completedRoutes,
        long attendanceEvents,
        long auditEvents) {
}
