package serp.project.school_bus_service.service;

import serp.project.school_bus_service.shared.base.IBaseService;

import serp.project.school_bus_service.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.dto.request.BatchAttendanceRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.BatchAttendanceResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripAttendanceManifestResponse;
import serp.project.school_bus_service.dto.response.TripAttendanceSummaryResponse;
import serp.project.school_bus_service.entity.AttendanceEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;

import java.util.List;

public interface IAttendanceService extends IBaseService<AttendanceEntity, Long> {

    PageResponse<AttendanceResponse> getAttendance(AttendanceParamsRequest params, Long tenantId);

    List<AttendanceResponse> getTripAttendance(Long tripId, Long tenantId);

    /**
     * System-generated NOT_SERVED event — called when a stop is skipped or trip cancelled.
     * Creates an AttendanceEntity with eventType=NOT_SERVED for audit trail.
     */
    void recordNotServedEvent(TripExecutionEntity trip, TripStudentEntity student,
                              RouteStopEntity routeStop, String reason, Long tenantId, Long actorId);

    TripAttendanceManifestResponse getTripAttendanceManifest(Long tripId, Long tenantId);

    TripAttendanceSummaryResponse getTripAttendanceSummary(Long tripId, Long tenantId);

    AttendanceResponse boardTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse dropoffTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId);

    AttendanceResponse markTripStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId);

    AttendanceResponse markTripStudentNoShow(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId);

    /** Internal: returns raw entities ordered by recordedAt desc, used for manifest building. */
    List<AttendanceEntity> findAttendancesByRoute(Long routeId, Long tenantId);

    BatchAttendanceResponse batchUpdateAttendance(Long tripId, Long routeStopId,
            BatchAttendanceRequest request, Long tenantId, Long actorId);

    long countByTenant(Long tenantId);
}
