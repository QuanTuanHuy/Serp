package serp.project.school_bus_service.dto.response;

public record ReportOverviewResponse(
        long totalRequests,
        long approvedRequests,
        long completedTrips,
        long attendanceEvents,
        long tripCount,
        long attendanceCount,
        long capacityCount) {
}
