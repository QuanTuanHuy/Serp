/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import serp.project.first_mile.caller.dto.DistanceMatrixResult;
import serp.project.first_mile.caller.dto.GeoPoint;
import serp.project.first_mile.enums.RoutingVehicle;

import java.util.List;

public interface DistanceMatrixCaller {

    DistanceMatrixResult calculateDistanceMatrix(
            List<GeoPoint> origins,
            List<GeoPoint> destinations,
            RoutingVehicle vehicle
    );
}
