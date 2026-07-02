package serp.project.school_bus_service.repository.projection;

public interface SchoolPickupPointSummaryProjection {

    Long getSchoolId();

    Integer getPickupPointCount();

    Boolean getAnyMissingCoordinates();
}
