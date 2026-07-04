package serp.project.school_bus_service.repository.projection;

import java.time.LocalDateTime;

public interface TripStopOperationProjection {

    Long getTripStopLogId();

    Long getRouteStopId();

    Integer getStopOrder();

    String getStopPurpose();

    String getLocationType();

    String getStopStatus();

    LocalDateTime getActualArrivalTime();

    LocalDateTime getActualDepartureTime();
}
