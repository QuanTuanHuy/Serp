package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.entity.AttendanceEntity;

import java.util.List;

public interface IAttendanceService extends IBaseService<AttendanceEntity, Long> {

    PageResponse<AttendanceResponse> getAttendance(AttendanceParamsRequest params, Long tenantId);

    AttendanceResponse checkIn(AttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse checkOut(AttendanceActionRequest request, Long tenantId, Long actorId);

    List<AttendanceResponse> getTripAttendance(Long tripId, Long tenantId);

    AttendanceResponse boardTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse dropoffTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markTripStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId);

    /** Internal: returns raw entities ordered by recordedAt desc, used for manifest building. */
    List<AttendanceEntity> findAttendancesByRoute(Long routeId, Long tenantId);

    long countByTenant(Long tenantId);
}
