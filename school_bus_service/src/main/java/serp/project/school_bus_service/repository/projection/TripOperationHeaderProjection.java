package serp.project.school_bus_service.repository.projection;

import java.time.LocalDate;

public interface TripOperationHeaderProjection {

    Long getTripId();

    String getTripCode();

    Long getRouteId();

    String getRouteCode();

    String getRouteName();

    String getRouteDirection();

    String getTripStatus();

    LocalDate getServiceDate();

    String getBusPlateNumber();

    String getDriverName();

    String getAttendantName();

    String getCancellationReason();

    String getRouteGeometry();

    Double getDistanceKm();

    Integer getDurationMin();
}
