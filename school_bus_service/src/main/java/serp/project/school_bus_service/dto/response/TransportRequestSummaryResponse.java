package serp.project.school_bus_service.dto.response;

public record TransportRequestSummaryResponse(
        long totalRequests,
        long submittedRequests,
        long approvedRequests,
        long rejectedRequests) {
}
