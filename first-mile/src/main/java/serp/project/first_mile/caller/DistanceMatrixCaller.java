/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import serp.project.first_mile.enums.RoutingVehicle;

import java.util.List;

public interface DistanceMatrixCaller {

    DistanceMatrixResult calculateDistanceMatrix(
            List<GeoPoint> origins,
            List<GeoPoint> destinations,
            RoutingVehicle vehicle
    );

    record GeoPoint(Double latitude, Double longitude) {
    }

    record DistanceMatrixResult(List<List<DistanceMatrixElement>> rows) {
    }

    record DistanceMatrixElement(String status, Long durationSeconds, Long distanceMeters) {
        public boolean isOk() {
            return "OK".equalsIgnoreCase(status)
                    && durationSeconds != null && durationSeconds >= 0
                    && distanceMeters != null && distanceMeters >= 0;
        }
    }
}
