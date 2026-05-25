package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.DemoEventLogResponse;
import serp.project.school_bus_service.dto.response.DemoSessionResponse;
import serp.project.school_bus_service.entity.AttendanceEntity;
import serp.project.school_bus_service.entity.DemoEventLogEntity;
import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.shared.base.BaseMapper;

import java.util.List;

@Component
public class OperationsMapper extends BaseMapper {

    public AttendanceResponse toAttendanceResponse(AttendanceEntity entity) {
        AttendanceResponse r = enrich(new AttendanceResponse(), entity);
        r.setRouteId(entity.getRoute().getId());
        r.setRouteCode(entity.getRoute().getRouteCode());
        r.setTripId(entity.getTrip() == null ? null : entity.getTrip().getId());
        r.setRouteStopId(entity.getRouteStop() == null ? null : entity.getRouteStop().getId());
        r.setStudentId(entity.getStudent().getId());
        r.setStudentName(entity.getStudent().getFullName());
        r.setAttendanceType(entity.getAttendanceType().name());
        r.setEventType(entity.getEventType() == null ? null : entity.getEventType().name());
        r.setEventSource(entity.getEventSource() == null ? null : entity.getEventSource().name());
        r.setStatus(entity.getStatus().name());
        r.setRecordedAt(entity.getRecordedAt());
        r.setRecordedBy(entity.getRecordedBy());
        r.setNotes(entity.getNotes());
        return r;
    }

    public DemoSessionResponse toDemoSessionResponse(DemoSessionEntity entity, List<DemoEventLogEntity> events) {
        DemoSessionResponse r = enrich(new DemoSessionResponse(), entity);
        r.setDemoCode(entity.getDemoCode());
        r.setTripId(entity.getTrip().getId());
        r.setTripCode(entity.getTrip().getTripCode());
        r.setStatus(entity.getStatus().name());
        r.setSpeedMultiplier(entity.getSpeedMultiplier());
        r.setCurrentStopOrder(entity.getCurrentStopOrder());
        r.setCurrentLatitude(entity.getCurrentLatitude());
        r.setCurrentLongitude(entity.getCurrentLongitude());
        r.setProgressPercent(entity.getProgressPercent());
        r.setStartedAt(entity.getStartedAt());
        r.setPausedAt(entity.getPausedAt());
        r.setCompletedAt(entity.getCompletedAt());
        r.setEvents(mapList(events, this::toDemoEventLogResponse));
        return r;
    }

    public DemoEventLogResponse toDemoEventLogResponse(DemoEventLogEntity entity) {
        DemoEventLogResponse r = enrich(new DemoEventLogResponse(), entity);
        r.setDemoSessionId(entity.getDemoSession().getId());
        r.setEventType(entity.getEventType());
        r.setEventTime(entity.getEventTime());
        r.setPayloadJson(entity.getPayloadJson());
        return r;
    }
}
