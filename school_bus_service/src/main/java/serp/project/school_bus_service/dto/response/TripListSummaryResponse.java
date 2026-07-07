package serp.project.school_bus_service.dto.response;

public record TripListSummaryResponse(
        long totalTrips,
        long inProgressTrips,
        long completedTrips) {
}
