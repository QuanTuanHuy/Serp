/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.caller;

import serp.project.tms_order.caller.dto.GeoPoint;

import java.util.Optional;

public interface GeocodeCaller {
    Optional<GeoPoint> searchFirst(String query);
}
