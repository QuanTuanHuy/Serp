package serp.project.school_bus_service.repository.projection;

public interface TripStopProgressRowProjection {

    Long getTripId();

    Long getRouteStopId();

    Integer getStopOrder();

    String getStatus();

    String getStopName();

    String getLocationType();
}
