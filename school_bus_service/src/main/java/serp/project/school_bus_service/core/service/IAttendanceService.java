package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.application.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.application.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;

import java.util.List;

public interface IAttendanceService {

    PageResponse<AttendanceResponse> getAttendance(AttendanceParamsRequest params, Long tenantId);

    AttendanceResponse checkIn(AttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse checkOut(AttendanceActionRequest request, Long tenantId, Long actorId);

    List<AttendanceResponse> getTripAttendance(Long tripId, Long tenantId);

    AttendanceResponse boardTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse dropoffTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markTripStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId);
}
