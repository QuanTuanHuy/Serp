package serp.project.second_mile.caller;

import serp.project.second_mile.caller.dto.GeoPoint;

import java.util.Optional;

public interface GeocodeCaller {
    Optional<GeoPoint> searchFirst(String query);


}
