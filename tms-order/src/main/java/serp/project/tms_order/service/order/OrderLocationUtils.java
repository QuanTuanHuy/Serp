/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.order;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import serp.project.tms_order.domain.Dimension;

public final class OrderLocationUtils {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private OrderLocationUtils() {
    }

    public static Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public static Dimension buildDimensions(Double length, Double width, Double height) {
        if (length == null || width == null || height == null) {
            return null;
        }

        Dimension dimensions = new Dimension();
        dimensions.setLength(length);
        dimensions.setWidth(width);
        dimensions.setHeight(height);
        return dimensions;
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

    public static Double toLatitude(Point location) {
        return location == null ? null : location.getY();
    }

    public static Double toLongitude(Point location) {
        return location == null ? null : location.getX();
    }
}
