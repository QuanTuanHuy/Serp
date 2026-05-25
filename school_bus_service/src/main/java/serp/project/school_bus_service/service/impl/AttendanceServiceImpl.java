package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.dto.request.AttendanceActionRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.IStudentService;
import serp.project.school_bus_service.service.ITransportRequestService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.service.ITripStudentService;
import serp.project.school_bus_service.enums.AttendanceStatus;
import serp.project.school_bus_service.enums.AttendanceType;
import serp.project.school_bus_service.enums.AttendanceEventType;
import serp.project.school_bus_service.enums.EventSource;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.AttendanceEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.repository.AttendanceRepository;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AttendanceServiceImpl extends AbstractBaseService<AttendanceEntity, Long> implements IAttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final IRouteService routeService;
    private final ITripExecutionService tripExecutionService;
    private final IStudentService studentService;
    private final ITransportRequestService transportRequestService;
    private final IAuditLogService auditLogService;
    private final IRouteStopService routeStopService;
    private final ITripStudentService tripStudentService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;


    public AttendanceServiceImpl(
    AttendanceRepository attendanceRepository,
                                 @Lazy IRouteService routeService,
                                 ITripExecutionService tripExecutionService,
                                 IStudentService studentService,
                                 ITransportRequestService transportRequestService,
                                 IAuditLogService auditLogService,
                                 IRouteStopService routeStopService,
                                 ITripStudentService tripStudentService,
                                 SchoolBusMapper mapper,
                                 MessageCommon messageCommon) {
        this.attendanceRepository = attendanceRepository;
        this.routeService = routeService;
        this.tripExecutionService = tripExecutionService;
        this.studentService = studentService;
        this.transportRequestService = transportRequestService;
        this.auditLogService = auditLogService;
        this.routeStopService = routeStopService;
        this.tripStudentService = tripStudentService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


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
            throw new AppException(AppErrorCode.Attendance.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Attendance.INVALID_STATE));
        }

        if (!student.getSchool().getId().equals(route.getSchool().getId())) {
            throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST, messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
        }

        if (!transportRequestService.hasApprovedRequestForStudent(student.getId(), route.getSchool().getId(),
                route.getServiceDate(), tenantId)) {
            throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST, messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
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
        TripStudentEntity tripStudent = tripStudentService
                .findByTripAndStudent(tripId, request.getStudentId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.Attendance.STUDENT_NOT_IN_TRIP, messageCommon.getMessage(AppErrorCode.Attendance.STUDENT_NOT_IN_TRIP)));
        RouteStopEntity routeStop = routeStopService
                .findRouteStop(request.getRouteStopId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, messageCommon.getMessage(AppErrorCode.NOT_FOUND)));
        List<AttendanceEntity> previousEvents = attendanceRepository
                .findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(tripId,
                        request.getStudentId(), tenantId);
        if (eventType == AttendanceEventType.BOARDED && previousEvents.stream()
                .anyMatch(event -> event.getEventType() == AttendanceEventType.BOARDED)) {
            throw new AppException(AppErrorCode.Attendance.ALREADY_BOARDED, messageCommon.getMessage(AppErrorCode.Attendance.ALREADY_BOARDED));
        }
        if (eventType == AttendanceEventType.DROPPED_OFF && previousEvents.stream()
                .noneMatch(event -> event.getEventType() == AttendanceEventType.BOARDED)) {
            throw new AppException(AppErrorCode.Attendance.MUST_BOARD_FIRST, messageCommon.getMessage(AppErrorCode.Attendance.MUST_BOARD_FIRST));
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
        tripStudentService.save(tripStudent);

        AttendanceEntity saved = attendanceRepository.save(attendance);
        auditLogService.log(tenantId, actorId, "TripAttendance", saved.getId(), eventType.name(),
                "Recorded trip attendance event");
        return mapper.toAttendanceResponse(saved);
    }

    private Specification<AttendanceEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }

    @Override
    public List<AttendanceEntity> findAttendancesByRoute(Long routeId, Long tenantId) {
        return attendanceRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(routeId, tenantId);
    }

    @Override
    public long countByTenant(Long tenantId) {
        return attendanceRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
