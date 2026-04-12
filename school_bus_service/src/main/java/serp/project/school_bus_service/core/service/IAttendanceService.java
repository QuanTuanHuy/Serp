package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.application.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;

public interface IAttendanceService {

    PageResponse<AttendanceResponse> getAttendance(AttendanceParamsRequest params, Long tenantId);

    AttendanceResponse checkIn(AttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse checkOut(AttendanceActionRequest request, Long tenantId, Long actorId);
}
