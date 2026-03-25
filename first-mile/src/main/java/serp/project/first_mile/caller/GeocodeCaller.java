/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import java.util.Optional;

public interface GeocodeCaller {
    Optional<GeoPoint> searchFirst(String query);

    record GeoPoint(Double latitude, Double longitude) {
    }
}
