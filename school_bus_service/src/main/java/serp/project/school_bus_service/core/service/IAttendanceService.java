package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;

import java.util.List;

public interface IAttendanceService {

    List<AttendanceResponse> getAttendance(Long tenantId);

    AttendanceResponse checkIn(AttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse checkOut(AttendanceActionRequest request, Long tenantId, Long actorId);
}
