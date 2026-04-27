package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.application.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.application.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.application.dto.response.AttendanceResponse;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.core.service.IAttendanceService;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IRouteService;
import serp.project.school_bus_service.core.service.IStudentService;
import serp.project.school_bus_service.core.service.ITransportRequestService;
import serp.project.school_bus_service.core.service.ITripExecutionService;
import serp.project.school_bus_service.enums.AttendanceStatus;
import serp.project.school_bus_service.enums.AttendanceType;
import serp.project.school_bus_service.enums.AttendanceEventType;
import serp.project.school_bus_service.enums.EventSource;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripExecutionEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripStudentEntity;
import serp.project.school_bus_service.infrastructure.store.repository.AttendanceRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RouteStopRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripStudentRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl extends AbstractBaseService<AttendanceEntity, Long> implements IAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final IRouteService routeService;
    private final ITripExecutionService tripExecutionService;
    private final IStudentService studentService;
    private final ITransportRequestService transportRequestService;
    private final IAuditLogService auditLogService;
    private final RouteStopRepository routeStopRepository;
    private final TripStudentRepository tripStudentRepository;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<AttendanceEntity, Long> getRepository() {
        return attendanceRepository;
    }

    @Override
    public PageResponse<AttendanceResponse> getAttendance(AttendanceParamsRequest params, Long tenantId) {
        return PageResponse.from(attendanceRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "route.routeCode", "student.fullName",
                        "attendanceType", "status", "notes"),
                pageable(params, Set.of("id", "recordedAt", "attendanceType", "status", "createdAt", "updatedAt"),
                        "recordedAt")),
                mapper::toAttendanceResponse);
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(AttendanceActionRequest request, Long tenantId, Long actorId) {
        return recordAttendance(request, tenantId, actorId, AttendanceType.CHECKED_IN);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(AttendanceActionRequest request, Long tenantId, Long actorId) {
        return recordAttendance(request, tenantId, actorId, AttendanceType.CHECKED_OUT);
    }

    @Override
    public List<AttendanceResponse> getTripAttendance(Long tripId, Long tenantId) {
        tripExecutionService.getTripEntity(tripId, tenantId);
        return attendanceRepository.findByTripIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(tripId, tenantId)
                .stream()
                .map(mapper::toAttendanceResponse)
                .toList();
    }

    @Override
    @Transactional
    public AttendanceResponse boardTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId) {
        return recordTripAttendance(tripId, request, tenantId, actorId, AttendanceEventType.BOARDED);
    }

    @Override
    @Transactional
    public AttendanceResponse dropoffTripStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId) {
        return recordTripAttendance(tripId, request, tenantId, actorId, AttendanceEventType.DROPPED_OFF);
    }

    @Override
    @Transactional
    public AttendanceResponse markTripStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId) {
        return recordTripAttendance(tripId, request, tenantId, actorId, AttendanceEventType.ABSENT);
    }

    private AttendanceResponse recordAttendance(AttendanceActionRequest request, Long tenantId, Long actorId,
            AttendanceType attendanceType) {
        RoutePlanEntity route = routeService.getRouteEntity(request.getRouteId(), tenantId);
        StudentEntity student = studentService.getStudent(request.getStudentId(), tenantId);

        if (route.getStatus() != RouteStatus.IN_PROGRESS && route.getStatus() != RouteStatus.ASSIGNED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        if (!student.getSchool().getId().equals(route.getSchool().getId())) {
            throw new AppException(AppErrorCode.INVALID_REQUEST);
        }

        if (!transportRequestService.hasApprovedRequestForStudent(student.getId(), route.getSchool().getId(),
                route.getServiceDate(), tenantId)) {
            throw new AppException(AppErrorCode.INVALID_REQUEST);
        }

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.markCreated(tenantId, actor(actorId));
        attendance.setRoute(route);
        attendance.setStudent(student);
        attendance.setAttendanceType(attendanceType);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setRecordedAt(LocalDateTime.now());
        attendance.setRecordedBy(actorId);
        attendance.setNotes(request.getNotes());
        AttendanceEntity saved = attendanceRepository.save(attendance);
        auditLogService.log(tenantId, actorId, "Attendance", saved.getId(), attendanceType.name(),
                "Recorded " + attendanceType.name().toLowerCase());
        return mapper.toAttendanceResponse(saved);
    }

    private AttendanceResponse recordTripAttendance(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId, AttendanceEventType eventType) {
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        TripStudentEntity tripStudent = tripStudentRepository
                .findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalse(tripId, request.getStudentId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.INVALID_REQUEST, "Student is not in this trip"));
        RouteStopEntity routeStop = routeStopRepository
                .findByIdAndTenantIdAndIsDeletedFalse(request.getRouteStopId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        List<AttendanceEntity> previousEvents = attendanceRepository
                .findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(tripId,
                        request.getStudentId(), tenantId);
        if (eventType == AttendanceEventType.BOARDED && previousEvents.stream()
                .anyMatch(event -> event.getEventType() == AttendanceEventType.BOARDED)) {
            throw new AppException(AppErrorCode.CONFLICT, "Student has already boarded this trip");
        }
        if (eventType == AttendanceEventType.DROPPED_OFF && previousEvents.stream()
                .noneMatch(event -> event.getEventType() == AttendanceEventType.BOARDED)) {
            throw new AppException(AppErrorCode.INVALID_STATE, "Student must board before dropoff");
        }

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.markCreated(tenantId, actor(actorId));
        attendance.setRoute(trip.getRoute());
        attendance.setTrip(trip);
        attendance.setRouteStop(routeStop);
        attendance.setStudent(tripStudent.getStudent());
        attendance.setAttendanceType(eventType == AttendanceEventType.DROPPED_OFF
                ? AttendanceType.CHECKED_OUT
                : AttendanceType.CHECKED_IN);
        attendance.setEventType(eventType);
        attendance.setEventSource(EventSource.MANUAL);
        attendance.setStatus(eventType == AttendanceEventType.ABSENT ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT);
        attendance.setRecordedAt(LocalDateTime.now());
        attendance.setRecordedBy(actorId);
        attendance.setNotes(request.getNotes());

        tripStudent.setStatus(switch (eventType) {
            case BOARDED -> TripStudentStatus.BOARDED;
            case DROPPED_OFF -> TripStudentStatus.DROPPED_OFF;
            case ABSENT -> TripStudentStatus.ABSENT;
            case NO_SHOW -> TripStudentStatus.NO_SHOW;
        });
        tripStudent.markUpdated(actor(actorId));
        tripStudentRepository.save(tripStudent);

        AttendanceEntity saved = attendanceRepository.save(attendance);
        auditLogService.log(tenantId, actorId, "TripAttendance", saved.getId(), eventType.name(),
                "Recorded trip attendance event");
        return mapper.toAttendanceResponse(saved);
    }

    private Specification<AttendanceEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            serp.project.school_bus_service.application.dto.request.BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }
}
