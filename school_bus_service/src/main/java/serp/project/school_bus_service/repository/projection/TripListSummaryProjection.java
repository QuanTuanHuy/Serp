package serp.project.school_bus_service.repository.projection;

public interface TripListSummaryProjection {
    Long getTotalTrips();

    Long getInProgressTrips();

    Long getCompletedTrips();
}
