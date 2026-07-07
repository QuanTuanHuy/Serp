package serp.project.school_bus_service.repository.projection;

public interface TransportRequestSummaryProjection {
    Long getTotalRequests();

    Long getSubmittedRequests();

    Long getApprovedRequests();

    Long getRejectedRequests();
}
