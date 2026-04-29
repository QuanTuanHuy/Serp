/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.dto.request.CreateHubRequest;
import serp.project.second_mile.dto.request.UpdateHubRequest;
import serp.project.second_mile.dto.response.HubResponse;

public final class HubMapper {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private HubMapper() {
    }

    public static Hub toEntity(CreateHubRequest request) {
        Hub hub = new Hub();
        hub.setCode(request.getCode());
        hub.setName(request.getName());
        hub.setHubType(request.getHubType());
        hub.setProvinceCode(request.getProvinceCode());
        hub.setWardCode(request.getWardCode());
        hub.setAddressDetail(request.getAddressDetail());
        hub.setPhoneNumber(request.getPhoneNumber());
        hub.setWorkingStartTime(request.getWorkingStartTime());
        hub.setWorkingEndTime(request.getWorkingEndTime());
        if (request.getDailyCapacity() != null) {
            hub.setDailyCapacity(request.getDailyCapacity());
        }
        if (request.getCurrentLoad() != null) {
            hub.setCurrentLoad(request.getCurrentLoad());
        }
        hub.setStatus(request.getStatus());
        hub.setLocation(toPoint(request.getLatitude(), request.getLongitude()));
        return hub;
    }

    public static void mapForUpdate(UpdateHubRequest request, Hub hub) {
        hub.setCode(request.getCode());
        hub.setName(request.getName());
        hub.setHubType(request.getHubType());
        hub.setProvinceCode(request.getProvinceCode());
        hub.setWardCode(request.getWardCode());
        hub.setAddressDetail(request.getAddressDetail());
        hub.setPhoneNumber(request.getPhoneNumber());
        hub.setWorkingStartTime(request.getWorkingStartTime());
        hub.setWorkingEndTime(request.getWorkingEndTime());
        hub.setDailyCapacity(request.getDailyCapacity());
        hub.setCurrentLoad(request.getCurrentLoad());
        hub.setStatus(request.getStatus());
        hub.setLocation(toPoint(request.getLatitude(), request.getLongitude()));
    }

    public static HubResponse toResponse(Hub hub) {
        return new HubResponse(
                hub.getId(),
                hub.getCode(),
                hub.getName(),
                hub.getHubType(),
                hub.getProvinceCode(),
                hub.getWardCode(),
                hub.getAddressDetail(),
                hub.getPhoneNumber(),
                hub.getImageUrl(),
                hub.getWorkingStartTime(),
                hub.getWorkingEndTime(),
                hub.getDailyCapacity(),
                hub.getCurrentLoad(),
                toLatitude(hub.getLocation()),
                toLongitude(hub.getLocation()),
                hub.getStatus(),
                hub.getVersion(),
                hub.getCreatedAt(),
                hub.getUpdatedAt(),
                hub.getCreatedBy(),
                hub.getUpdatedBy(),
                hub.getTenantId()
        );
    }

    private static Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    private static Double toLatitude(Point location) {
        return location == null ? null : location.getY();
    }

    private static Double toLongitude(Point location) {
        return location == null ? null : location.getX();
    }
}
