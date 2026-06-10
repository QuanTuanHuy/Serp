package serp.project.school_bus_service.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.AttendanceParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.TripAttendanceManifestResponse;
import serp.project.school_bus_service.dto.response.TripAttendanceSummaryResponse;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.service.ITripStopLogService;
import serp.project.school_bus_service.service.ITripStudentService;
import serp.project.school_bus_service.enums.AttendanceStatus;
import serp.project.school_bus_service.enums.AttendanceType;
import serp.project.school_bus_service.enums.AttendanceEventType;
import serp.project.school_bus_service.enums.EventSource;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.enums.TripStopStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.AttendanceEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
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
    private final ITripExecutionService tripExecutionService;
    private final IAuditLogService auditLogService;
    private final IRouteStopService routeStopService;
    private final ITripStudentService tripStudentService;
    private final ITripStopLogService tripStopLogService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusDataScopeService schoolBusDataScopeService;


    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            @Lazy ITripExecutionService tripExecutionService,
            IAuditLogService auditLogService,
            IRouteStopService routeStopService,
            ITripStudentService tripStudentService,
            ITripStopLogService tripStopLogService,
            SchoolBusMapper mapper,
            MessageCommon messageCommon,
            ISchoolBusDataScopeService schoolBusDataScopeService) {
        this.attendanceRepository = attendanceRepository;
        this.tripExecutionService = tripExecutionService;
        this.auditLogService = auditLogService;
        this.routeStopService = routeStopService;
        this.tripStudentService = tripStudentService;
        this.tripStopLogService = tripStopLogService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusDataScopeService = schoolBusDataScopeService;
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
    public List<AttendanceResponse> getTripAttendance(Long tripId, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessAttendance(tripId);
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

    private AttendanceResponse recordTripAttendance(Long tripId, TripAttendanceActionRequest request, Long tenantId,
            Long actorId, AttendanceEventType eventType) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
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

        // DEPOT terminal stops never have attendance actions
        if (routeStop.getLocationType() == RouteLocationType.DEPOT) {
            throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
        }

        // Stop must be ARRIVED or BOARDING to record attendance
        TripStopLogEntity stopLog = tripStopLogService
                .findByTripAndRouteStop(tripId, request.getRouteStopId(), tenantId)
                .orElse(null);
        if (stopLog == null
                || (stopLog.getStatus() != TripStopStatus.ARRIVED
                        && stopLog.getStatus() != TripStopStatus.BOARDING)) {
            throw new AppException(AppErrorCode.Attendance.STOP_NOT_ACTIVE,
                    messageCommon.getMessage(AppErrorCode.Attendance.STOP_NOT_ACTIVE));
        }

        TripStudentStatus currentStatus = tripStudent.getStatus();
        boolean isOutbound = trip.getRouteDirection() != null
                && "OUTBOUND".equals(trip.getRouteDirection().name());

        // ── Stop-purpose / event-type validation ──────────────────────────────
        switch (eventType) {
            case BOARDED -> {
                if (isOutbound) {
                    // OUTBOUND: board at a PICKUP stop only
                    if (routeStop.getStopPurpose() != RouteStopPurpose.PICKUP) {
                        throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                                messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
                    }
                    if (tripStudent.getPickupStop() != null
                            && !tripStudent.getPickupStop().getId().equals(request.getRouteStopId())) {
                        throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                                messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
                    }
                } else {
                    // RETURN: board at SCHOOL START_TERMINAL only
                    if (routeStop.getStopPurpose() != RouteStopPurpose.START_TERMINAL
                            || routeStop.getLocationType() != RouteLocationType.SCHOOL) {
                        throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                                messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
                    }
                }
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
                if (isOutbound) {
                    // OUTBOUND: drop off at SCHOOL END_TERMINAL only; student must be BOARDED
                    if (routeStop.getStopPurpose() != RouteStopPurpose.END_TERMINAL
                            || routeStop.getLocationType() != RouteLocationType.SCHOOL) {
                        throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                                messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
                    }
                    if (currentStatus != TripStudentStatus.BOARDED) {
                        throw new AppException(AppErrorCode.Attendance.MUST_BOARD_FIRST,
                                messageCommon.getMessage(AppErrorCode.Attendance.MUST_BOARD_FIRST));
                    }
                } else {
                    // RETURN: drop off at a DROPOFF stop; student must be BOARDED
                    if (routeStop.getStopPurpose() != RouteStopPurpose.DROPOFF) {
                        throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                                messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
                    }
                    if (tripStudent.getDropoffStop() != null
                            && !tripStudent.getDropoffStop().getId().equals(request.getRouteStopId())) {
                        throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                                messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
                    }
                    if (currentStatus != TripStudentStatus.BOARDED) {
                        throw new AppException(AppErrorCode.Attendance.MUST_BOARD_FIRST,
                                messageCommon.getMessage(AppErrorCode.Attendance.MUST_BOARD_FIRST));
                    }
                }
            }
            case ABSENT, NO_SHOW -> {
                // ABSENT/NO_SHOW are only valid from PLANNED state
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
            // NOT_SERVED is system-generated via recordNotServedEvent(); should never reach here.
            case NOT_SERVED -> TripStudentStatus.NOT_SERVED;
        });
        tripStudent.markUpdated(actor(actorId));
        tripStudentService.save(tripStudent);

        // Update the stop log counts for BOARDED / DROPPED_OFF events
        // (reuse the stopLog already fetched for the status validation above)
        if (eventType == AttendanceEventType.BOARDED) {
            stopLog.setActualBoardedCount(stopLog.getActualBoardedCount() + 1);
            stopLog.markUpdated(actor(actorId));
            tripStopLogService.save(stopLog);
        } else if (eventType == AttendanceEventType.DROPPED_OFF) {
            stopLog.setActualDroppedCount(stopLog.getActualDroppedCount() + 1);
            stopLog.markUpdated(actor(actorId));
            tripStopLogService.save(stopLog);
        }

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
    @Transactional
    public void recordNotServedEvent(TripExecutionEntity trip, TripStudentEntity student,
                                     RouteStopEntity routeStop, String reason, Long tenantId, Long actorId) {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.markCreated(tenantId, actor(null)); // system actor
        attendance.setRoute(trip.getRoute());
        attendance.setTrip(trip);
        attendance.setRouteStop(routeStop);
        attendance.setStudent(student.getStudent());
        attendance.setAttendanceType(AttendanceType.CHECKED_IN);
        attendance.setEventType(AttendanceEventType.NOT_SERVED);
        attendance.setEventSource(EventSource.SYSTEM);
        attendance.setStatus(AttendanceStatus.ABSENT);
        attendance.setRecordedAt(LocalDateTime.now());
        attendance.setRecordedBy(actorId);
        attendance.setNotes(reason);
        attendanceRepository.save(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public TripAttendanceSummaryResponse getTripAttendanceSummary(Long tripId, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessAttendance(tripId);
        tripExecutionService.getTripEntity(tripId, tenantId);
        List<serp.project.school_bus_service.entity.TripStudentEntity> students =
                tripStudentService.findByTrip(tripId, tenantId);
        TripAttendanceSummaryResponse summary = new TripAttendanceSummaryResponse();
        summary.setTotalStudents(students.size());
        summary.setPlanned((int) students.stream().filter(s -> s.getStatus() == TripStudentStatus.PLANNED).count());
        summary.setBoarded((int) students.stream().filter(s -> s.getStatus() == TripStudentStatus.BOARDED).count());
        summary.setDroppedOff((int) students.stream().filter(s -> s.getStatus() == TripStudentStatus.DROPPED_OFF).count());
        summary.setAbsent((int) students.stream().filter(s -> s.getStatus() == TripStudentStatus.ABSENT).count());
        summary.setNoShow((int) students.stream().filter(s -> s.getStatus() == TripStudentStatus.NO_SHOW).count());
        summary.setNotServed((int) students.stream().filter(s -> s.getStatus() == TripStudentStatus.NOT_SERVED).count());
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public TripAttendanceManifestResponse getTripAttendanceManifest(Long tripId, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessAttendance(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        List<serp.project.school_bus_service.entity.TripStudentEntity> tripStudents =
                tripStudentService.findByTrip(tripId, tenantId);
        List<serp.project.school_bus_service.entity.TripStopLogEntity> stopLogs =
                tripStopLogService.findByTrip(tripId, tenantId);

        // Summary
        TripAttendanceSummaryResponse summary = new TripAttendanceSummaryResponse();
        summary.setTotalStudents(tripStudents.size());
        summary.setPlanned((int) tripStudents.stream().filter(s -> s.getStatus() == TripStudentStatus.PLANNED).count());
        summary.setBoarded((int) tripStudents.stream().filter(s -> s.getStatus() == TripStudentStatus.BOARDED).count());
        summary.setDroppedOff((int) tripStudents.stream().filter(s -> s.getStatus() == TripStudentStatus.DROPPED_OFF).count());
        summary.setAbsent((int) tripStudents.stream().filter(s -> s.getStatus() == TripStudentStatus.ABSENT).count());
        summary.setNoShow((int) tripStudents.stream().filter(s -> s.getStatus() == TripStudentStatus.NO_SHOW).count());
        summary.setNotServed((int) tripStudents.stream().filter(s -> s.getStatus() == TripStudentStatus.NOT_SERVED).count());

        boolean isOutbound = trip.getRouteDirection() != null
                && trip.getRouteDirection().name().equals("OUTBOUND");

        // Build stop items
        java.util.Map<Long, java.util.List<serp.project.school_bus_service.entity.TripStudentEntity>> boardingByStop =
                new java.util.HashMap<>();
        java.util.Map<Long, java.util.List<serp.project.school_bus_service.entity.TripStudentEntity>> dropoffByStop =
                new java.util.HashMap<>();
        for (serp.project.school_bus_service.entity.TripStudentEntity ts : tripStudents) {
            if (ts.getPickupStop() != null) {
                boardingByStop.computeIfAbsent(ts.getPickupStop().getId(), k -> new java.util.ArrayList<>()).add(ts);
            }
            if (ts.getDropoffStop() != null) {
                dropoffByStop.computeIfAbsent(ts.getDropoffStop().getId(), k -> new java.util.ArrayList<>()).add(ts);
            }
        }

        java.util.List<TripAttendanceManifestResponse.TripAttendanceStopItem> stops = stopLogs.stream()
                .sorted(java.util.Comparator.comparing(serp.project.school_bus_service.entity.TripStopLogEntity::getStopOrder))
                .map(sl -> {
                    TripAttendanceManifestResponse.TripAttendanceStopItem item =
                            new TripAttendanceManifestResponse.TripAttendanceStopItem();
                    RouteStopEntity rs = sl.getRouteStop();
                    item.setRouteStopId(rs.getId());
                    item.setStopOrder(sl.getStopOrder());
                    item.setLocationType(rs.getLocationType() != null ? rs.getLocationType().name() : null);
                    item.setStopPurpose(rs.getStopPurpose() != null ? rs.getStopPurpose().name() : null);
                    item.setDisplayName(rs.getDisplayName());
                    item.setStopStatus(sl.getStatus().name());
                    item.setActualBoardedCount(sl.getActualBoardedCount() != null ? sl.getActualBoardedCount() : 0);
                    item.setActualDroppedCount(sl.getActualDroppedCount() != null ? sl.getActualDroppedCount() : 0);
                    item.setLatitude(rs.getLatitude());
                    item.setLongitude(rs.getLongitude());
                    item.setActualArrivalTime(sl.getActualArrivalTime() != null ? sl.getActualArrivalTime().toString() : null);
                    item.setActualDepartureTime(sl.getActualDepartureTime() != null ? sl.getActualDepartureTime().toString() : null);

                    // Boarding count depends on purpose
                    // OUTBOUND PICKUP: students with pickupStopId == this stop
                    // RETURN START_TERMINAL (school): all PLANNED students (board here)
                    // OUTBOUND END_TERMINAL (school): all BOARDED students (drop here)
                    // RETURN DROPOFF: students with dropoffStopId == this stop
                    if (rs.getStopPurpose() == RouteStopPurpose.END_TERMINAL
                            && rs.getLocationType() == RouteLocationType.SCHOOL
                            && isOutbound) {
                        // OUTBOUND school terminal: count BOARDED students (they drop off here)
                        long boardedCount = tripStudents.stream()
                                .filter(s -> s.getStatus() == TripStudentStatus.BOARDED
                                        || s.getStatus() == TripStudentStatus.DROPPED_OFF).count();
                        item.setPlannedDropoffCount((int) boardedCount);
                        item.setPlannedBoardingCount(0);
                    } else if (rs.getStopPurpose() == RouteStopPurpose.START_TERMINAL
                            && rs.getLocationType() == RouteLocationType.SCHOOL
                            && !isOutbound) {
                        // RETURN school terminal: count PLANNED students (they board here)
                        item.setPlannedBoardingCount(tripStudents.size());
                        item.setPlannedDropoffCount(0);
                    } else {
                        java.util.List<serp.project.school_bus_service.entity.TripStudentEntity> boarding =
                                boardingByStop.getOrDefault(rs.getId(), java.util.List.of());
                        java.util.List<serp.project.school_bus_service.entity.TripStudentEntity> dropping =
                                dropoffByStop.getOrDefault(rs.getId(), java.util.List.of());
                        item.setPlannedBoardingCount(boarding.size());
                        item.setPlannedDropoffCount(dropping.size());
                    }

                    int studentCount = 0;
                    if (rs.getStopPurpose() == RouteStopPurpose.END_TERMINAL && rs.getLocationType() == RouteLocationType.SCHOOL && isOutbound) {
                        studentCount = item.getPlannedDropoffCount();
                    } else if (rs.getStopPurpose() == RouteStopPurpose.START_TERMINAL && rs.getLocationType() == RouteLocationType.SCHOOL && !isOutbound) {
                        studentCount = item.getPlannedBoardingCount();
                    } else {
                        studentCount = item.getPlannedBoardingCount() + item.getPlannedDropoffCount();
                    }
                    item.setStudentCount(studentCount);

                    return item;
                })
                .toList();

        // Build student items
        java.util.List<TripAttendanceManifestResponse.TripAttendanceStudentItem> students = tripStudents.stream()
                .map(ts -> {
                    TripAttendanceManifestResponse.TripAttendanceStudentItem item =
                            new TripAttendanceManifestResponse.TripAttendanceStudentItem();
                    item.setTripStudentId(ts.getId());
                    item.setStudentId(ts.getStudent() != null ? ts.getStudent().getId() : null);
                    item.setStudentName(ts.getStudent() != null ? ts.getStudent().getFullName() : null);
                    item.setStudentCode(ts.getStudent() != null ? ts.getStudent().getStudentCode() : null);
                    item.setStatus(ts.getStatus().name());
                    item.setPickupStopId(ts.getPickupStop() != null ? ts.getPickupStop().getId() : null);
                    item.setDropoffStopId(ts.getDropoffStop() != null ? ts.getDropoffStop().getId() : null);
                    item.setSubscriptionId(ts.getSubscription() != null ? ts.getSubscription().getId() : null);
                    item.setNote(ts.getNote());
                    return item;
                })
                .toList();

        TripAttendanceManifestResponse response = new TripAttendanceManifestResponse();
        response.setTripId(trip.getId());
        response.setTripCode(trip.getTripCode());
        response.setRouteId(trip.getRoute() != null ? trip.getRoute().getId() : null);
        response.setRouteCode(trip.getRoute() != null ? trip.getRoute().getRouteCode() : null);
        response.setRouteName(trip.getRoute() != null ? trip.getRoute().getRouteName() : null);
        response.setRouteDirection(trip.getRouteDirection() != null ? trip.getRouteDirection().name() : null);
        response.setTripStatus(trip.getStatus().name());
        response.setServiceDate(trip.getServiceDate() != null ? trip.getServiceDate().toString() : null);
        response.setRouteGeometry(trip.getRouteGeometryPath() != null ? trip.getRouteGeometryPath() : (trip.getRoute() != null ? trip.getRoute().getGeometryPath() : null));
        response.setDistanceKm(trip.getPlannedDistanceKm() != null ? trip.getPlannedDistanceKm() : (trip.getRoute() != null ? trip.getRoute().getPlannedDistanceKm() : null));
        response.setDurationMin(trip.getPlannedDurationMin() != null ? trip.getPlannedDurationMin() : (trip.getRoute() != null ? trip.getRoute().getPlannedDurationMin() : null));
        response.setSummary(summary);
        response.setStops(stops);
        response.setStudents(students);
        return response;

    }

    @Override
    public long countByTenant(Long tenantId) {
        return attendanceRepository.countByTenantIdAndIsDeletedFalse(tenantId);
    }
}
