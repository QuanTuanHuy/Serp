package serp.project.school_bus_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.application.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.application.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseMapper;

import java.util.List;

@Component
public class RouteMapper extends BaseMapper {

    public RoutePlanResponse toRoutePlanResponse(RoutePlanEntity entity) {
        RoutePlanResponse response = enrich(new RoutePlanResponse(), entity);
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setSchoolLatitude(entity.getSchool().getLatitude());
        response.setSchoolLongitude(entity.getSchool().getLongitude());
        response.setRouteDirection(entity.getRouteDirection().name());
        response.setStartLocationType(entity.getStartLocationType().name());
        applyStartLocation(response, entity);
        response.setEndLocationType(entity.getEndLocationType().name());
        applyEndLocation(response, entity);
        response.setRouteCode(entity.getRouteCode());
        response.setRouteName(entity.getRouteName());
        response.setServiceDate(entity.getServiceDate());
        response.setShiftType(entity.getShiftType().name());
        response.setStatus(entity.getStatus().name());
        response.setPlannedDistanceKm(entity.getPlannedDistanceKm());
        response.setPlannedDurationMin(entity.getPlannedDurationMin());
        response.setPlanningNotes(entity.getPlanningNotes());
        response.setGeometryPath(entity.getGeometryPath());
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());
        return response;
    }

    public RouteStopResponse toRouteStopResponse(RouteStopEntity entity) {
        RouteStopResponse response = enrich(new RouteStopResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setPickupPointId(entity.getPickupPoint().getId());
        response.setPickupPointName(entity.getPickupPoint().getName());
        response.setPickupPointAddress(entity.getPickupPoint().getAddress());
        response.setPickupPointLatitude(entity.getPickupPoint().getLatitude());
        response.setPickupPointLongitude(entity.getPickupPoint().getLongitude());
        response.setStopType(entity.getStopType().name());
        response.setStopOrder(entity.getStopOrder());
        response.setEstimatedStudentCount(entity.getEstimatedStudentCount());
        response.setPlannedArrivalTime(entity.getPlannedArrivalTime());
        response.setPlannedDepartureTime(entity.getPlannedDepartureTime());
        return response;
    }

    public RouteAssignmentResponse toRouteAssignmentResponse(RouteAssignmentEntity entity) {
        if (entity == null) {
            return null;
        }
        RouteAssignmentResponse response = enrich(new RouteAssignmentResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setBusId(entity.getBus().getId());
        response.setBusPlateNumber(entity.getBus().getPlateNumber());
        response.setDriverId(entity.getDriver().getId());
        response.setDriverName(entity.getDriver().getFullName());
        response.setAttendantId(entity.getAttendant() == null ? null : entity.getAttendant().getId());
        response.setAttendantName(entity.getAttendant() == null ? null : entity.getAttendant().getFullName());
        response.setAssignedAt(entity.getAssignedAt());
        return response;
    }

    public RouteDetailResponse toRouteDetailResponse(RoutePlanEntity route, List<RouteStopEntity> stops,
            RouteAssignmentEntity assignment) {
        RouteDetailResponse response = new RouteDetailResponse();
        response.setRoute(toRoutePlanResponse(route));
        response.setStops(mapList(stops, this::toRouteStopResponse));
        response.setAssignment(toRouteAssignmentResponse(assignment));
        return response;
    }

    private void applyStartLocation(RoutePlanResponse response, RoutePlanEntity entity) {
        if (entity.getStartSchool() != null) {
            response.setStartLocationId(entity.getStartSchool().getId());
            response.setStartLocationName(entity.getStartSchool().getName());
            response.setStartLocationAddress(entity.getStartSchool().getAddress());
            response.setStartLocationLatitude(entity.getStartSchool().getLatitude());
            response.setStartLocationLongitude(entity.getStartSchool().getLongitude());
            return;
        }
        if (entity.getStartDepot() != null) {
            response.setStartLocationId(entity.getStartDepot().getId());
            response.setStartLocationName(entity.getStartDepot().getName());
            response.setStartLocationAddress(entity.getStartDepot().getAddress());
            response.setStartLocationLatitude(entity.getStartDepot().getLatitude());
            response.setStartLocationLongitude(entity.getStartDepot().getLongitude());
        }
    }

    private void applyEndLocation(RoutePlanResponse response, RoutePlanEntity entity) {
        if (entity.getEndSchool() != null) {
            response.setEndLocationId(entity.getEndSchool().getId());
            response.setEndLocationName(entity.getEndSchool().getName());
            response.setEndLocationAddress(entity.getEndSchool().getAddress());
            response.setEndLocationLatitude(entity.getEndSchool().getLatitude());
            response.setEndLocationLongitude(entity.getEndSchool().getLongitude());
            return;
        }
        if (entity.getEndDepot() != null) {
            response.setEndLocationId(entity.getEndDepot().getId());
            response.setEndLocationName(entity.getEndDepot().getName());
            response.setEndLocationAddress(entity.getEndDepot().getAddress());
            response.setEndLocationLatitude(entity.getEndDepot().getLatitude());
            response.setEndLocationLongitude(entity.getEndDepot().getLongitude());
        }
    }
}
