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
import serp.project.school_bus_service.service.ITripStopLogService;
import serp.project.school_bus_service.service.ITripStudentService;
import serp.project.school_bus_service.enums.AttendanceStatus;
import serp.project.school_bus_service.enums.AttendanceType;
import serp.project.school_bus_service.enums.AttendanceEventType;
import serp.project.school_bus_service.enums.EventSource;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStatus;
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
    private final ITripStopLogService tripStopLogService;
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
                                 ITripStopLogService tripStopLogService,
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
        this.tripStopLogService = tripStopLogService;
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

    @Override
    @Transactional
    public AttendanceResponse markTripStudentNoShow(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId) {
        return recordTripAttendance(tripId, request, tenantId, actorId, AttendanceEventType.NO_SHOW);
    }

    private AttendanceResponse recordAttendance(AttendanceActionRequest request, Long tenantId, Long actorId,
            AttendanceType attendanceType) {
        RoutePlanEntity route = routeService.getRouteEntity(request.getRouteId(), tenantId);
        StudentEntity student = studentService.getStudent(request.getStudentId(), tenantId);

        // Allow attendance recording when a trip is being executed (TRIP_CREATED)
        // or the route is already assigned (ASSIGNED) but trip not yet started.
        if (route.getStatus() != RouteStatus.TRIP_CREATED && route.getStatus() != RouteStatus.ASSIGNED) {
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

        // Trip must be IN_PROGRESS to record attendance
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Attendance.INVALID_STATE,
                    messageCommon.getMessage(AppErrorCode.Attendance.INVALID_STATE));
        }

        TripStudentEntity tripStudent = tripStudentService
                .findByTripAndStudent(tripId, request.getStudentId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.Attendance.STUDENT_NOT_IN_TRIP,
                        messageCommon.getMessage(AppErrorCode.Attendance.STUDENT_NOT_IN_TRIP)));
        RouteStopEntity routeStop = routeStopService
                .findRouteStop(request.getRouteStopId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        messageCommon.getMessage(AppErrorCode.NOT_FOUND)));

        // Validate the stop belongs to this trip's route
        if (!routeStop.getRoute().getId().equals(trip.getRoute().getId())) {
            throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
        }

        // Validate that the requested stop matches the student's planned pickup or dropoff stop.
        // This prevents recording attendance at the wrong stop due to operator error.
        if (eventType == AttendanceEventType.BOARDED
                && tripStudent.getPickupStop() != null
                && !tripStudent.getPickupStop().getId().equals(request.getRouteStopId())) {
            throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
        }
        if (eventType == AttendanceEventType.DROPPED_OFF
                && tripStudent.getDropoffStop() != null
                && !tripStudent.getDropoffStop().getId().equals(request.getRouteStopId())) {
            throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
        }

        // Status-based duplicate guard: use TripStudent.status as the single source of truth.
        // This prevents duplicate board/dropoff/absent/no-show events that would corrupt counts.
        TripStudentStatus currentStatus = tripStudent.getStatus();
        boolean isOutbound = trip.getRouteDirection() != null
                && "OUTBOUND".equals(trip.getRouteDirection().name());
        switch (eventType) {
            case BOARDED -> {
                if (currentStatus == TripStudentStatus.BOARDED) {
                    throw new AppException(AppErrorCode.Attendance.ALREADY_BOARDED,
                            messageCommon.getMessage(AppErrorCode.Attendance.ALREADY_BOARDED));
                }
                if (currentStatus != TripStudentStatus.PLANNED) {
                    throw new AppException(AppErrorCode.Attendance.STUDENT_STATUS_INVALID,
                            messageCommon.getMessage(AppErrorCode.Attendance.STUDENT_STATUS_INVALID));
                }
            }
            case DROPPED_OFF -> {
                // For OUTBOUND: student must have boarded first.
                // For RETURN: the trip starts at school, so students begin as PLANNED (not yet explicitly boarded at school).
                //   We allow dropoff from PLANNED or BOARDED because the school boarding stop is not yet modeled as a
                //   separate TripStop. Once school terminal boarding is modeled, this should require BOARDED only.
                // TODO: enforce BOARDED-only when RETURN school boarding stop is modeled.
                if (isOutbound && currentStatus != TripStudentStatus.BOARDED) {
                    throw new AppException(AppErrorCode.Attendance.MUST_BOARD_FIRST,
                            messageCommon.getMessage(AppErrorCode.Attendance.MUST_BOARD_FIRST));
                }
                if (!isOutbound
                        && currentStatus != TripStudentStatus.BOARDED
                        && currentStatus != TripStudentStatus.PLANNED) {
                    throw new AppException(AppErrorCode.Attendance.STUDENT_STATUS_INVALID,
                            messageCommon.getMessage(AppErrorCode.Attendance.STUDENT_STATUS_INVALID));
                }
            }
            case ABSENT, NO_SHOW -> {
                // ABSENT/NO_SHOW are only valid for students still in PLANNED state.
                // NOT_SERVED, BOARDED, DROPPED_OFF, ABSENT, NO_SHOW are all terminal — reject.
                if (currentStatus != TripStudentStatus.PLANNED) {
                    throw new AppException(AppErrorCode.Attendance.STUDENT_STATUS_INVALID,
                            messageCommon.getMessage(AppErrorCode.Attendance.STUDENT_STATUS_INVALID));
                }
            }
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
        attendance.setStatus(eventType == AttendanceEventType.ABSENT || eventType == AttendanceEventType.NO_SHOW
                ? AttendanceStatus.ABSENT
                : AttendanceStatus.PRESENT);
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

        // Update the stop log counts for BOARDED / DROPPED_OFF events
        tripStopLogService.findByTripAndRouteStop(tripId, request.getRouteStopId(), tenantId)
                .ifPresent(stopLog -> {
                    if (eventType == AttendanceEventType.BOARDED) {
                        stopLog.setActualBoardedCount(stopLog.getActualBoardedCount() + 1);
                        stopLog.markUpdated(actor(actorId));
                        tripStopLogService.save(stopLog);
                    } else if (eventType == AttendanceEventType.DROPPED_OFF) {
                        stopLog.setActualDroppedCount(stopLog.getActualDroppedCount() + 1);
                        stopLog.markUpdated(actor(actorId));
                        tripStopLogService.save(stopLog);
                    }
                });

        AttendanceEntity saved = attendanceRepository.save(attendance);
        auditLogService.log(tenantId, actorId, "TripAttendance", saved.getId(), eventType.name(),
                "Recorded trip attendance event");
        // TODO notification: notify parents/guardians of the attendance event (eventType).
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
