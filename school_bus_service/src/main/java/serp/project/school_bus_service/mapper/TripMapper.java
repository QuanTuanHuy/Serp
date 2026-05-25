package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.dto.response.TripHistoryResponse;
import serp.project.school_bus_service.dto.response.TripStopLogResponse;
import serp.project.school_bus_service.dto.response.TripStudentResponse;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripHistoryEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.shared.base.BaseMapper;

import java.util.List;

@Component
public class TripMapper extends BaseMapper {

    public TripHistoryResponse toTripHistoryResponse(TripHistoryEntity entity) {
        TripHistoryResponse response = enrich(new TripHistoryResponse(), entity);
        response.setRouteId(entity.getRoute().getId());
        response.setRouteCode(entity.getRouteCode());
        response.setServiceDate(entity.getServiceDate());
        response.setStatus(entity.getStatus());
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());
        response.setBusId(entity.getBus() == null ? null : entity.getBus().getId());
        response.setBusPlateNumber(entity.getBus() == null ? null : entity.getBus().getPlateNumber());
        response.setDriverId(entity.getDriver() == null ? null : entity.getDriver().getId());
        response.setDriverName(entity.getDriver() == null ? null : entity.getDriver().getFullName());
        response.setAttendantId(entity.getAttendant() == null ? null : entity.getAttendant().getId());
        response.setAttendantName(entity.getAttendant() == null ? null : entity.getAttendant().getFullName());
        return response;
    }

    public TripExecutionResponse toTripExecutionResponse(TripExecutionEntity entity, List<TripStopLogEntity> stops,
            List<TripStudentEntity> students) {
        TripExecutionResponse response = enrich(new TripExecutionResponse(), entity);
        response.setTripCode(entity.getTripCode());
        response.setRouteId(entity.getRoute().getId());
        response.setRouteCode(entity.getRoute().getRouteCode());
        response.setRouteName(entity.getRoute().getRouteName());
        response.setServiceDate(entity.getServiceDate());
        response.setRouteDirection(entity.getRouteDirection().name());
        response.setShiftType(entity.getShiftType().name());
        response.setStatus(entity.getStatus().name());
        response.setPlannedStartAt(entity.getPlannedStartAt());
        response.setPlannedEndAt(entity.getPlannedEndAt());
        response.setStartedAt(entity.getStartedAt());
        response.setCompletedAt(entity.getCompletedAt());
        response.setPlannedDistanceKm(entity.getPlannedDistanceKm());
        response.setPlannedDurationMin(entity.getPlannedDurationMin());
        response.setActualDistanceKm(entity.getActualDistanceKm());
        response.setActualDurationMin(entity.getActualDurationMin());
        response.setCompletionNote(entity.getCompletionNote());
        response.setSimulationMode(entity.getSimulationMode());
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
        response.setStopName(entity.getRouteStop().getPickupPoint().getName());
        response.setStopOrder(entity.getStopOrder());
        response.setStatus(entity.getStatus().name());
        response.setActualArrivalTime(entity.getActualArrivalTime());
        response.setActualDepartureTime(entity.getActualDepartureTime());
        response.setDelayMinutes(entity.getDelayMinutes());
        response.setActualBoardedCount(entity.getActualBoardedCount());
        response.setActualDroppedCount(entity.getActualDroppedCount());
        response.setNote(entity.getNote());
        return response;
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
