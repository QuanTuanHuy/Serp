package serp.project.school_bus_service.repository.projection;

public interface SchoolRegistrySummaryProjection {
    Long getTotalSchools();

    Long getTotalPickupPoints();

    Long getLinkedPickupPoints();

    Long getMissingCoordinates();
}
