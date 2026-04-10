package serp.project.first_mile.service.dto;

import java.util.ArrayList;
import java.util.List;

public record RouteState(
        Long courierStaffId,
        String courierCode,
        String courierName,
        Integer maxStops,
        Long vehicleId,
        String vehicleLicensePlate,
        double maxWeight,
        double maxVolume,
        double depotLatitude,
        double depotLongitude,
        List<PickupOrderNode> stops
) {
    public RouteState copy() {
        return new RouteState(
                courierStaffId,
                courierCode,
                courierName,
                maxStops,
                vehicleId,
                vehicleLicensePlate,
                maxWeight,
                maxVolume,
                depotLatitude,
                depotLongitude,
                new ArrayList<>(stops)
        );
    }
}
