/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller;

import serp.project.first_mile.caller.dto.GeoPoint;

import java.util.Optional;

public interface GeocodeCaller {
    Optional<GeoPoint> searchFirst(String query);
}
