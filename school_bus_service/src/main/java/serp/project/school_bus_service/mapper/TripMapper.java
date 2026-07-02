package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.dto.response.TripStopLogResponse;
import serp.project.school_bus_service.dto.response.TripStudentResponse;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.shared.base.BaseMapper;

import java.util.List;

@Component
public class TripMapper extends BaseMapper {

    public TripExecutionResponse toTripExecutionResponse(TripExecutionEntity entity, List<TripStopLogEntity> stops,
            List<TripStudentEntity> students) {
        TripExecutionResponse response = enrich(new TripExecutionResponse(), entity);
        response.setTripCode(entity.getTripCode());
        response.setRouteId(entity.getRoute().getId());
        response.setRouteCode(entity.getRoute().getRouteCode());
        response.setRouteName(entity.getRoute().getRouteName());
        response.setServiceDate(entity.getServiceDate());
        response.setRouteDirection(entity.getRouteDirection() == null ? null : entity.getRouteDirection().name());
        response.setStatus(entity.getStatus().name());
        response.setPlannedStartAt(entity.getPlannedStartAt());
        response.setPlannedEndAt(entity.getPlannedEndAt());
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());
        response.setCompletionNote(entity.getCompletionNote());
        response.setCancelledAt(entity.getCancelledAt());
        response.setCancelledBy(entity.getCancelledBy());
        response.setCancellationReason(entity.getCancellationReason());
        response.setBusId(entity.getBus() == null ? null : entity.getBus().getId());

        response.setBusPlateNumber(entity.getBus() == null ? null : entity.getBus().getPlateNumber());
        response.setDriverId(entity.getDriver() == null ? null : entity.getDriver().getId());
        response.setDriverName(entity.getDriver() == null ? null : entity.getDriver().getFullName());
        response.setAttendantId(entity.getAttendant() == null ? null : entity.getAttendant().getId());
        response.setAttendantName(entity.getAttendant() == null ? null : entity.getAttendant().getFullName());
        response.setStops(mapList(stops, this::toTripStopLogResponse));
        response.setStudents(mapList(students, this::toTripStudentResponse));
        // Start/end snapshot
        response.setStartLocationType(entity.getStartLocationType());
        if (entity.getStartSchool() != null) {
            response.setStartLocationName(entity.getStartSchool().getName());
        } else if (entity.getStartDepot() != null) {
            response.setStartLocationName(entity.getStartDepot().getName());
        }
        response.setEndLocationType(entity.getEndLocationType());
        if (entity.getEndSchool() != null) {
            response.setEndLocationName(entity.getEndSchool().getName());
        } else if (entity.getEndDepot() != null) {
            response.setEndLocationName(entity.getEndDepot().getName());
        }
        return response;
    }

    public TripStopLogResponse toTripStopLogResponse(TripStopLogEntity entity) {
        TripStopLogResponse response = enrich(new TripStopLogResponse(), entity);
        response.setTripId(entity.getTrip().getId());
        response.setRouteStopId(entity.getRouteStop().getId());
        response.setStopName(entity.getRouteStop().getDisplayName());
        response.setLocationId(entity.getRouteStop().getLocationId());
        response.setLocationType(entity.getRouteStop().getLocationType() == null
                ? null
                : entity.getRouteStop().getLocationType().name());
        response.setLocationName(entity.getRouteStop().getDisplayName());
        response.setLocationAddress(resolveRouteStopAddress(entity.getRouteStop()));
        response.setLatitude(entity.getRouteStop().getLatitude());
        response.setLongitude(entity.getRouteStop().getLongitude());
        response.setStopOrder(entity.getRouteStop().getStopOrder());
        response.setStatus(entity.getStatus().name());
        response.setActualArrivalTime(entity.getActualArrivalTime());
        response.setActualDepartureTime(entity.getActualDepartureTime());
        response.setDelayMinutes(entity.getDelayMinutes());
        response.setActualBoardedCount(entity.getActualBoardedCount());
        response.setActualDroppedCount(entity.getActualDroppedCount());
        response.setNote(entity.getNote());
        return response;
    }

    private String resolveRouteStopAddress(serp.project.school_bus_service.entity.RouteStopEntity stop) {
        if (stop.getPickupPoint() != null) {
            return stop.getPickupPoint().getAddress();
        }
        if (stop.getSchool() != null) {
            return stop.getSchool().getAddress();
        }
        if (stop.getDepot() != null) {
            return stop.getDepot().getAddress();
        }
        return null;
    }

    public TripStudentResponse toTripStudentResponse(TripStudentEntity entity) {
        TripStudentResponse response = enrich(new TripStudentResponse(), entity);
        response.setTripId(entity.getTrip().getId());
        response.setStudentId(entity.getStudent().getId());
        response.setStudentName(entity.getStudent().getFullName());
        response.setPickupStopId(entity.getPickupStop() == null ? null : entity.getPickupStop().getId());
        response.setDropoffStopId(entity.getDropoffStop() == null ? null : entity.getDropoffStop().getId());
        response.setSubscriptionId(entity.getSubscription() == null ? null : entity.getSubscription().getId());
        response.setStatus(entity.getStatus().name());
        response.setNote(entity.getNote());
        return response;
    }
}
