package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.entity.AttendanceEntity;
import serp.project.school_bus_service.shared.base.BaseMapper;

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
}
