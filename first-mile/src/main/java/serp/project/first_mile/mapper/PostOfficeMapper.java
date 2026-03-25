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
        postOffice.setCode(request.code());
        postOffice.setName(request.name());
        postOffice.setProvinceCode(request.provinceCode());
        postOffice.setWardCode(request.wardCode());
        postOffice.setAddressDetail(request.addressDetail());
        postOffice.setPhoneNumber(request.phoneNumber());
        postOffice.setServiceRadiusM(request.serviceRadiusM());
        postOffice.setDailyCapacity(request.dailyCapacity());
        postOffice.setCurrentLoad(request.currentLoad());
        postOffice.setPriority(request.priority());
        postOffice.setStatus(request.status());
        postOffice.setTenantId(request.tenantId());
        postOffice.setLocation(toPoint(request.latitude(), request.longitude()));
        return postOffice;
    }

    public static void mapForUpdate(UpdatePostOfficeRequest request, PostOffice postOffice) {
        postOffice.setCode(request.code());
        postOffice.setName(request.name());
        postOffice.setProvinceCode(request.provinceCode());
        postOffice.setWardCode(request.wardCode());
        postOffice.setAddressDetail(request.addressDetail());
        postOffice.setPhoneNumber(request.phoneNumber());
        postOffice.setServiceRadiusM(request.serviceRadiusM());
        postOffice.setDailyCapacity(request.dailyCapacity());
        postOffice.setCurrentLoad(request.currentLoad());
        postOffice.setPriority(request.priority());
        postOffice.setStatus(request.status());
        postOffice.setTenantId(request.tenantId());
        postOffice.setLocation(toPoint(request.latitude(), request.longitude()));
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
