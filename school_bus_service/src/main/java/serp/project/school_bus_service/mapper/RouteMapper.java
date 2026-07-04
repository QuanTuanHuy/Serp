package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.shared.base.BaseMapper;

import java.util.List;

@Component
public class RouteMapper extends BaseMapper {

    public RoutePlanResponse toRoutePlanResponse(RoutePlanEntity entity) {
        RoutePlanResponse response = enrich(new RoutePlanResponse(), entity);
        if (entity.getSchool() != null) {
            response.setSchoolId(entity.getSchool().getId());
            response.setSchoolName(entity.getSchool().getName());
            response.setSchoolLatitude(entity.getSchool().getLatitude());
            response.setSchoolLongitude(entity.getSchool().getLongitude());
        }
        response.setRouteDirection(entity.getRouteDirection() == null ? null : entity.getRouteDirection().name());
        response.setStartLocationType(entity.getStartLocationType() == null ? null : entity.getStartLocationType().name());
        applyStartLocation(response, entity);
        response.setEndLocationType(entity.getEndLocationType() == null ? null : entity.getEndLocationType().name());
        applyEndLocation(response, entity);
        response.setRouteCode(entity.getRouteCode());
        response.setRouteName(entity.getRouteName());
        response.setServiceDate(entity.getServiceDate());
        response.setStatus(entity.getStatus().name());
        response.setPlannedDistanceKm(entity.getPlannedDistanceKm());
        response.setPlannedDurationMin(entity.getPlannedDurationMin());
        response.setPlannedStudentCount(entity.getPlannedStudentCount());
        if (entity.getSelectedBus() != null) {
            response.setBusId(entity.getSelectedBus().getId());
            response.setBusPlateNumber(entity.getSelectedBus().getPlateNumber());
            response.setBusName(entity.getSelectedBus().getPlateNumber());
            response.setBusCapacity(entity.getSelectedBus().getCapacity());
            response.setBusStatus(entity.getSelectedBus().getStatus());
        }
        if (entity.getStartDepot() != null) {
            response.setStartDepotName(entity.getStartDepot().getName());
        } else if (entity.getEndDepot() != null) {
            response.setStartDepotName(entity.getEndDepot().getName());
        }
        response.setPlanningNotes(entity.getPlanningNotes());
        response.setGeometryPath(entity.getGeometryPath());
        response.setGeometrySource(entity.getGeometrySource() != null
                ? entity.getGeometrySource().name() : null);
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());
        response.setRequiredCapacity(entity.getRequiredCapacity());
        if (entity.getPlanningSession() != null) {
            response.setPlanningSessionId(entity.getPlanningSession().getId());
        }
        return response;
    }

    public RouteStopResponse toRouteStopResponse(RouteStopEntity entity) {
        RouteStopResponse response = enrich(new RouteStopResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setLocationType(entity.getLocationType() != null ? entity.getLocationType().name() : null);
        response.setStopPurpose(entity.getStopPurpose() != null ? entity.getStopPurpose().name() : null);
        response.setDisplayName(entity.getDisplayName());
        response.setLocationId(entity.getLocationId());
        response.setLocationName(entity.getDisplayName());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        if (entity.getPickupPoint() != null) {
            response.setLocationAddress(entity.getPickupPoint().getAddress());
            response.setPickupPointId(entity.getPickupPoint().getId());
            response.setPickupPointName(entity.getPickupPoint().getName());
            response.setPickupPointAddress(entity.getPickupPoint().getAddress());
            response.setPickupPointLatitude(entity.getPickupPoint().getLatitude());
            response.setPickupPointLongitude(entity.getPickupPoint().getLongitude());
        }
        if (entity.getSchool() != null) {
            response.setLocationAddress(entity.getSchool().getAddress());
            response.setSchoolId(entity.getSchool().getId());
            response.setSchoolName(entity.getSchool().getName());
        }
        if (entity.getDepot() != null) {
            response.setLocationAddress(entity.getDepot().getAddress());
            response.setDepotId(entity.getDepot().getId());
            response.setDepotName(entity.getDepot().getName());
        }
        response.setStopOrder(entity.getStopOrder());
        response.setEstimatedStudentCount(entity.getEstimatedStudentCount());
        response.setDistanceFromPreviousKm(entity.getDistanceFromPreviousKm());
        response.setEstimatedTravelTimeFromPrevious(entity.getEstimatedTravelTimeFromPrevious());
        return response;
    }

    public RouteAssignmentResponse toRouteAssignmentResponse(RouteAssignmentEntity entity) {
        if (entity == null) {
            return null;
        }
        RouteAssignmentResponse response = enrich(new RouteAssignmentResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setRouteStatus(entity.getRoute().getStatus().name());
        response.setBusId(entity.getBus().getId());
        response.setBusPlateNumber(entity.getBus().getPlateNumber());
        response.setBusCapacity(entity.getBus().getCapacity());
        response.setDriverId(entity.getDriver() == null ? null : entity.getDriver().getId());
        response.setDriverName(entity.getDriver() == null ? null : entity.getDriver().getFullName());




        response.setAttendantId(entity.getAttendant() == null ? null : entity.getAttendant().getId());
        response.setAttendantName(entity.getAttendant() == null ? null : entity.getAttendant().getFullName());
        response.setStatus(entity.getStatus() == null ? null : entity.getStatus().name());
        response.setAssignedBy(entity.getAssignedBy());
        response.setAssignmentNote(entity.getAssignmentNote());
        response.setAssignedAt(entity.getAssignedAt());
        response.setConfirmedAt(entity.getConfirmedAt());
        return response;
    }

    public RoutePlanStudentResponse toRoutePlanStudentResponse(RoutePlanStudentEntity entity) {
        RoutePlanStudentResponse response = new RoutePlanStudentResponse();
        response.setId(entity.getId());
        response.setRouteId(entity.getRoute().getId());
        response.setStudentId(entity.getStudent().getId());
        response.setStudentName(entity.getStudent().getFullName());
        response.setSubscriptionId(entity.getSubscription().getId());
        if (entity.getPickupStop() != null) {
            response.setPickupStopId(entity.getPickupStop().getId());
            response.setPickupStopName(entity.getPickupStop().getDisplayName());
        }
        if (entity.getDropoffStop() != null) {
            response.setDropoffStopId(entity.getDropoffStop().getId());
            response.setDropoffStopName(entity.getDropoffStop().getDisplayName());
        }
        return response;
    }

    public RouteDetailResponse toRouteDetailResponse(RoutePlanEntity route, List<RouteStopEntity> stops,
            List<RoutePlanStudentEntity> students, RouteAssignmentEntity assignment) {
        RouteDetailResponse response = new RouteDetailResponse();
        response.setRoute(toRoutePlanResponse(route));
        response.setStops(mapList(stops, this::toRouteStopResponse));
        response.setStudents(mapList(students, this::toRoutePlanStudentResponse));
        response.setAssignment(toRouteAssignmentResponse(assignment));
        return response;
    }

    public RouteDetailResponse toRouteDetailResponse(RoutePlanEntity route, List<RouteStopEntity> stops,
            RouteAssignmentEntity assignment) {
        RouteDetailResponse response = new RouteDetailResponse();
        response.setRoute(toRoutePlanResponse(route));
        response.setStops(mapList(stops, this::toRouteStopResponse));
        response.setStudents(List.of());
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
