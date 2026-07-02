/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kernel.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeoPointUtils {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private GeoPointUtils() {
    }

    public static Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public static boolean isValidCoordinate(double latitude, double longitude) {
        return !Double.isNaN(latitude)
                && !Double.isNaN(longitude)
                && !Double.isInfinite(latitude)
                && !Double.isInfinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    public static double distanceMeters(
            double fromLatitude,
            double fromLongitude,
            double toLatitude,
            double toLongitude
    ) {
        return HaversineUtils.distanceKm(fromLatitude, fromLongitude, toLatitude, toLongitude) * 1000.0;
    }

    public static Double latitude(Point point) {
        if (point == null) {
            return null;
        }
        return point.getY();
    }

    public static Double longitude(Point point) {
        if (point == null) {
            return null;
        }
        return point.getX();
    }

    public static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
