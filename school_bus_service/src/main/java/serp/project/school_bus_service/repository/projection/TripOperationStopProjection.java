package serp.project.school_bus_service.repository.projection;

public interface TripOperationStopProjection {

    Long getRouteStopId();

    Integer getStopOrder();

    String getLocationType();

    String getStopPurpose();

    Long getLocationId();

    String getLocationName();

    String getLocationAddress();

    Double getLatitude();

    Double getLongitude();

    String getStopStatus();

    Integer getActualBoardedCount();

    Integer getActualDroppedCount();

    String getActualArrivalTime();

    String getActualDepartureTime();

    Integer getPlannedBoardingCount();

    Integer getPlannedDropoffCount();
}
