/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.mapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.request.CreatePostOfficeRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeRequest;
import serp.project.first_mile.dto.response.PostOfficeResponse;

public final class PostOfficeMapper {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private PostOfficeMapper() {
    }

    public static PostOffice toEntity(CreatePostOfficeRequest request) {
        PostOffice postOffice = new PostOffice();
        postOffice.setCode(request.getCode());
        postOffice.setName(request.getName());
        postOffice.setProvinceCode(request.getProvinceCode());
        postOffice.setWardCode(request.getWardCode());
        postOffice.setAddressDetail(request.getAddressDetail());
        postOffice.setPhoneNumber(request.getPhoneNumber());
        postOffice.setOperationalStartDate(request.getOperationalStartDate());
        postOffice.setOperationalEndDate(request.getOperationalEndDate());
        postOffice.setWorkingStartTime(request.getWorkingStartTime());
        postOffice.setWorkingEndTime(request.getWorkingEndTime());
        postOffice.setServiceRadiusM(request.getServiceRadiusM());
        if (request.getDailyCapacity() != null) {
            postOffice.setDailyCapacity(request.getDailyCapacity());
        }
        if (request.getCurrentLoad() != null) {
            postOffice.setCurrentLoad(request.getCurrentLoad());
        }
        if (request.getPriority() != null) {
            postOffice.setPriority(request.getPriority());
        }
        postOffice.setStatus(request.getStatus());
        postOffice.setLocation(toPoint(request.getLatitude(), request.getLongitude()));
        return postOffice;
    }

    public static void mapForUpdate(UpdatePostOfficeRequest request, PostOffice postOffice) {
        postOffice.setCode(request.getCode());
        postOffice.setName(request.getName());
        postOffice.setProvinceCode(request.getProvinceCode());
        postOffice.setWardCode(request.getWardCode());
        postOffice.setAddressDetail(request.getAddressDetail());
        postOffice.setPhoneNumber(request.getPhoneNumber());
        postOffice.setOperationalStartDate(request.getOperationalStartDate());
        postOffice.setOperationalEndDate(request.getOperationalEndDate());
        postOffice.setWorkingStartTime(request.getWorkingStartTime());
        postOffice.setWorkingEndTime(request.getWorkingEndTime());
        postOffice.setServiceRadiusM(request.getServiceRadiusM());
        postOffice.setDailyCapacity(request.getDailyCapacity());
        postOffice.setCurrentLoad(request.getCurrentLoad());
        postOffice.setPriority(request.getPriority());
        postOffice.setStatus(request.getStatus());
        postOffice.setLocation(toPoint(request.getLatitude(), request.getLongitude()));
    }

    public static PostOfficeResponse toResponse(PostOffice postOffice) {
        return new PostOfficeResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                postOffice.getProvinceCode(),
                postOffice.getWardCode(),
                postOffice.getAddressDetail(),
                postOffice.getPhoneNumber(),
                postOffice.getOperationalStartDate(),
                postOffice.getOperationalEndDate(),
                postOffice.getWorkingStartTime(),
                postOffice.getWorkingEndTime(),
                postOffice.getServiceRadiusM(),
                postOffice.getDailyCapacity(),
                postOffice.getCurrentLoad(),
                postOffice.getPriority(),
                toLatitude(postOffice.getLocation()),
                toLongitude(postOffice.getLocation()),
                postOffice.getStatus(),
                postOffice.getVersion(),
                postOffice.getCreatedAt(),
                postOffice.getUpdatedAt(),
                postOffice.getCreatedBy(),
                postOffice.getUpdatedBy(),
                postOffice.getTenantId()
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
