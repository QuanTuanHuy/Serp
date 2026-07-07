package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.request.BatchAttendanceRequest;
import serp.project.school_bus_service.dto.request.CancelTripRequest;
import serp.project.school_bus_service.dto.request.CompleteTripRequest;
import serp.project.school_bus_service.dto.request.SkipStopRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.BatchAttendanceResponse;
import serp.project.school_bus_service.dto.response.TripOperationActionResponse;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.TripStopLogRepository;
import serp.project.school_bus_service.repository.TripStudentRepository;
import serp.project.school_bus_service.repository.projection.TripStopOperationProjection;
import serp.project.school_bus_service.service.*;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TripOperationServiceImpl implements ITripOperationService {

    private final ITripExecutionService tripExecutionService;
    private final ITripStopLogService tripStopLogService;
    private final ITripStudentService tripStudentService;
    private final IRouteStopService routeStopService;
    private final IAttendanceService attendanceService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusDataScopeService schoolBusDataScopeService;
    private final ISchoolBusDomainNotificationService domainNotificationService;
    private final TripStopLogRepository tripStopLogRepository;
    private final TripStudentRepository tripStudentRepository;

    public TripOperationServiceImpl(
            ITripExecutionService tripExecutionService,
            ITripStopLogService tripStopLogService,
            ITripStudentService tripStudentService,
            IRouteStopService routeStopService,
            @Lazy IAttendanceService attendanceService,
            SchoolBusMapper mapper,
            MessageCommon messageCommon,
            ISchoolBusDataScopeService schoolBusDataScopeService,
            ISchoolBusDomainNotificationService domainNotificationService,
            TripStopLogRepository tripStopLogRepository,
            TripStudentRepository tripStudentRepository) {
        this.tripExecutionService = tripExecutionService;
        this.tripStopLogService = tripStopLogService;
        this.tripStudentService = tripStudentService;
        this.routeStopService = routeStopService;
        this.attendanceService = attendanceService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusDataScopeService = schoolBusDataScopeService;
        this.domainNotificationService = domainNotificationService;
        this.tripStopLogRepository = tripStopLogRepository;
        this.tripStudentRepository = tripStudentRepository;
    }

    private String actor(Long actorId) {
        return actorId == null ? "SYSTEM" : actorId.toString();
    }

    private double calculateProgress(Long tripId, Long tenantId) {
        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(tripId, tenantId);
        long completed = stops.stream()
                .filter(s -> s.getStatus() == TripStopStatus.DEPARTED || s.getStatus() == TripStopStatus.SKIPPED)
                .count();
        return stops.isEmpty() ? 0.0 : ((double) completed / stops.size()) * 100.0;
    }

    @Override
    @Transactional
    public TripOperationActionResponse startTrip(Long tripId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.ASSIGNED && trip.getStatus() != TripStatus.PLANNED) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        TripStopOperationProjection firstStopProjection = tripStopLogRepository
                .findFirstOperationStop(trip.getId(), tenantId)
                .orElse(null);
        if (firstStopProjection == null) {
            throw new AppException(AppErrorCode.Trip.NO_STOPS, messageCommon.getMessage(AppErrorCode.Trip.NO_STOPS));
        }

        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartedAt(LocalDateTime.now());
        trip.markUpdated(actor(actorId));
        tripExecutionService.save(trip);

        TripStopLogEntity firstStop = findStopLogEntity(tripId, firstStopProjection.getRouteStopId(), tenantId);
        boolean isReturn = trip.getRouteDirection() == RouteDirection.RETURN;
        firstStop.setStatus(isReturn ? TripStopStatus.BOARDING : TripStopStatus.ARRIVED);
        firstStop.setActualArrivalTime(LocalDateTime.now());
        firstStop.markUpdated(actor(actorId));
        tripStopLogService.save(firstStop);

        domainNotificationService.notifyTripStarted(trip, actorId);
        return actionResponse(trip, firstStop, messageCommon.getMessage("trip.action.started"));
    }

    @Override
    @Transactional
    public TripOperationActionResponse arriveStop(Long tripId, Long routeStopId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        TripStopOperationProjection currentStop = findOperationStop(tripId, routeStopId, tenantId);
        TripStopLogEntity stopLog = findStopLogEntity(tripId, routeStopId, tenantId);

        if (parseStopStatus(currentStop) != TripStopStatus.PENDING) {
            throw new AppException(AppErrorCode.Trip.STOP_ALREADY_DONE, messageCommon.getMessage(AppErrorCode.Trip.STOP_ALREADY_DONE));
        }

        TripStopOperationProjection next = tripStopLogRepository
                .findFirstPendingOperationStop(tripId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE)));
        if (!next.getTripStopLogId().equals(currentStop.getTripStopLogId())) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        LocalDateTime now = LocalDateTime.now();
        stopLog.setStatus(TripStopStatus.ARRIVED);
        stopLog.setActualArrivalTime(now);
        stopLog.markUpdated(actor(actorId));
        tripStopLogService.save(stopLog);

        TripStopOperationProjection lastStop = tripStopLogRepository
                .findLastOperationStop(tripId, tenantId)
                .orElse(null);
        boolean isLastStop = lastStop != null && lastStop.getTripStopLogId().equals(currentStop.getTripStopLogId());

        if (isLastStop) {
            return completeTrip(tripId, null, tenantId, actorId);
        }

        return actionResponse(trip, stopLog, messageCommon.getMessage("trip.action.arrived"));
    }

    @Override
    @Transactional
    public TripOperationActionResponse startBoarding(Long tripId, Long routeStopId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        TripStopOperationProjection currentStop = findOperationStop(tripId, routeStopId, tenantId);
        TripStopLogEntity stopLog = findStopLogEntity(tripId, routeStopId, tenantId);

        if (parseStopStatus(currentStop) != TripStopStatus.ARRIVED) {
            throw new AppException(AppErrorCode.Trip.STOP_NOT_ARRIVED_BOARDING,
                    messageCommon.getMessage(AppErrorCode.Trip.STOP_NOT_ARRIVED_BOARDING));
        }

        boolean isOutbound = trip.getRouteDirection() == RouteDirection.OUTBOUND;
        RouteStopPurpose stopPurpose = parseStopPurpose(currentStop);

        if (isOutbound) {
            if (stopPurpose != RouteStopPurpose.PICKUP) {
                throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                        messageCommon.getMessage("trip.boarding.pickupOnly"));
            }
        } else {
            if (stopPurpose != RouteStopPurpose.DROPOFF) {
                throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                        messageCommon.getMessage("trip.dropoff.dropoffOnly"));
            }
        }

        if (countStudentsAtStop(tripId, routeStopId, tenantId, isOutbound) == 0) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                    messageCommon.getMessage(isOutbound ? "trip.boarding.pickupOnly" : "trip.dropoff.dropoffOnly"));
        }

        stopLog.setStatus(TripStopStatus.BOARDING);
        stopLog.markUpdated(actor(actorId));
        tripStopLogService.save(stopLog);

        return actionResponse(trip, stopLog, messageCommon.getMessage(
                isOutbound ? "trip.action.boardingStarted" : "trip.action.dropoffStarted"));
    }

    @Override
    @Transactional
    public TripOperationActionResponse departStop(Long tripId, Long routeStopId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        TripStopOperationProjection currentStop = findOperationStop(tripId, routeStopId, tenantId);
        TripStopLogEntity stopLog = findStopLogEntity(tripId, routeStopId, tenantId);
        TripStopStatus currentStatus = parseStopStatus(currentStop);

        if (currentStatus == TripStopStatus.DEPARTED || currentStatus == TripStopStatus.SKIPPED) {
            throw new AppException(AppErrorCode.Trip.STOP_ALREADY_DONE, messageCommon.getMessage(AppErrorCode.Trip.STOP_ALREADY_DONE));
        }
        if (currentStatus != TripStopStatus.ARRIVED && currentStatus != TripStopStatus.BOARDING) {
            throw new AppException(AppErrorCode.Trip.STOP_NOT_ARRIVED, messageCommon.getMessage(AppErrorCode.Trip.STOP_NOT_ARRIVED));
        }

        TripStopOperationProjection firstUnfinished = tripStopLogRepository
                .findFirstUnfinishedOperationStop(tripId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.Trip.INVALID_STATE,
                        messageCommon.getMessage("trip.depart.noActiveStop")));
        if (!firstUnfinished.getTripStopLogId().equals(currentStop.getTripStopLogId())) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                    messageCommon.getMessage("trip.depart.previousUnfinished"));
        }

        RouteStopPurpose stopPurpose = parseStopPurpose(currentStop);
        boolean isTerminal = stopPurpose != null && stopPurpose.isTerminal();

        if (!isTerminal) {
            boolean isOutbound = (trip.getRouteDirection() == RouteDirection.OUTBOUND);
            long stopStudentCount = countStudentsAtStop(tripId, routeStopId, tenantId, isOutbound);

            if (stopStudentCount > 0) {
                if (currentStatus != TripStopStatus.BOARDING) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                            messageCommon.getMessage("trip.depart.startBoardingFirst"));
                }

                long pendingCount = countPendingStudentsAtStop(tripId, routeStopId, tenantId, isOutbound);
                if (pendingCount > 0) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                            messageCommon.getMessage("trip.depart.pendingStudents"));
                }
            }
        }

        stopLog.setStatus(TripStopStatus.DEPARTED);
        stopLog.setActualDepartureTime(LocalDateTime.now());
        stopLog.markUpdated(actor(actorId));
        tripStopLogService.save(stopLog);

        return actionResponse(trip, stopLog, messageCommon.getMessage("trip.action.departed"));
    }

    @Override
    @Transactional
    public TripOperationActionResponse skipStop(Long tripId, Long routeStopId, SkipStopRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        TripStopLogEntity stopLog = tripStopLogService.findByTripAndRouteStop(tripId, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (stopLog.getStatus() == TripStopStatus.DEPARTED || stopLog.getStatus() == TripStopStatus.SKIPPED) {
            throw new AppException(AppErrorCode.Trip.STOP_ALREADY_DONE, messageCommon.getMessage(AppErrorCode.Trip.STOP_ALREADY_DONE));
        }

        RouteStopPurpose purpose = stopLog.getRouteStop() != null ? stopLog.getRouteStop().getStopPurpose() : null;
        if (purpose != null && purpose.isTerminal()) {
            throw new AppException(AppErrorCode.Trip.CANNOT_SKIP_TERMINAL,
                    messageCommon.getMessage(AppErrorCode.Trip.CANNOT_SKIP_TERMINAL));
        }

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new AppException(AppErrorCode.Trip.SKIP_REASON_REQUIRED, messageCommon.getMessage(AppErrorCode.Trip.SKIP_REASON_REQUIRED));
        }

        stopLog.setStatus(TripStopStatus.SKIPPED);
        stopLog.setNote(request.getReason());
        stopLog.markUpdated(actor(actorId));
        tripStopLogService.save(stopLog);

        // Mark PLANNED students as NOT_SERVED immediately
        boolean isOutbound = trip.getRouteDirection() == RouteDirection.OUTBOUND;
        List<TripStudentEntity> affectedStudents = tripStudentService.findByTrip(tripId, tenantId).stream()
                .filter(ts -> ts.getStatus() == TripStudentStatus.PLANNED)
                .filter(ts -> {
                    if (isOutbound) {
                        return ts.getPickupStop() != null && ts.getPickupStop().getId().equals(routeStopId);
                    } else {
                        return ts.getDropoffStop() != null && ts.getDropoffStop().getId().equals(routeStopId);
                    }
                })
                .toList();
        affectedStudents.forEach(ts -> {
                    ts.setStatus(TripStudentStatus.NOT_SERVED);
                    ts.setNote("Bỏ qua điểm dừng: " + request.getReason());
                    ts.markUpdated(actor(actorId));
                    tripStudentService.save(ts);
                    attendanceService.recordNotServedEvent(trip, ts, stopLog.getRouteStop(),
                            "Bỏ qua điểm dừng: " + request.getReason(), tenantId, actorId);
                });

        domainNotificationService.notifyStopSkipped(trip, affectedStudents, request.getReason(), actorId);
        return actionResponse(trip, stopLog, messageCommon.getMessage("trip.action.skipped"));
    }

    @Override
    @Transactional
    public TripOperationActionResponse completeTrip(Long tripId, CompleteTripRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        // All stops except the last stop (end terminal) must be DEPARTED or SKIPPED.
        // The last stop (end terminal) must be ARRIVED or DEPARTED.
        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(tripId, tenantId).stream()
                .sorted(Comparator.comparingInt(this::routeStopOrder))
                .toList();

        for (int i = 0; i < stops.size(); i++) {
            TripStopLogEntity stop = stops.get(i);
            boolean isEndTerminal = (i == stops.size() - 1);
            if (isEndTerminal) {
                if (stop.getStatus() != TripStopStatus.ARRIVED && stop.getStatus() != TripStopStatus.BOARDING && stop.getStatus() != TripStopStatus.DEPARTED) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                            messageCommon.getMessage("trip.complete.resolveStopsStudents"));
                }
            } else {
                if (stop.getStatus() != TripStopStatus.DEPARTED && stop.getStatus() != TripStopStatus.SKIPPED) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                            messageCommon.getMessage("trip.complete.resolveStopsStudents"));
                }
            }
        }

        // Auto-resolve PLANNED students whose stops were skipped
        boolean isOutboundComplete = trip.getRouteDirection() == RouteDirection.OUTBOUND;
        Set<Long> skippedRouteStopIds = tripStopLogService.findByTrip(tripId, tenantId).stream()
                .filter(s -> s.getStatus() == TripStopStatus.SKIPPED)
                .map(s -> s.getRouteStop().getId())
                .collect(Collectors.toSet());
        tripStudentService.findByTrip(tripId, tenantId).stream()
                .filter(ts -> ts.getStatus() == TripStudentStatus.PLANNED)
                .filter(ts -> {
                    if (isOutboundComplete) {
                        return ts.getPickupStop() != null && skippedRouteStopIds.contains(ts.getPickupStop().getId());
                    } else {
                        return ts.getDropoffStop() != null && skippedRouteStopIds.contains(ts.getDropoffStop().getId());
                    }
                })
                .forEach(ts -> {
                    ts.setStatus(TripStudentStatus.NOT_SERVED);
                    ts.setNote("Tự động đánh dấu chưa phục vụ: điểm dừng đã bị bỏ qua.");
                    ts.markUpdated(actor(actorId));
                    tripStudentService.save(ts);
                });

        // Auto-resolve BOARDED students to DROPPED_OFF for OUTBOUND trips upon completion
        if (isOutboundComplete) {
            tripStudentService.findByTrip(tripId, tenantId).stream()
                    .filter(ts -> ts.getStatus() == TripStudentStatus.BOARDED)
                    .forEach(ts -> {
                        ts.setStatus(TripStudentStatus.DROPPED_OFF);
                        ts.markUpdated(actor(actorId));
                        tripStudentService.save(ts);
                    });
        }

        // Ensure all students are processed
        boolean hasUnprocessedStudents = tripStudentService.findByTrip(tripId, tenantId).stream()
                .anyMatch(s -> s.getStatus() == TripStudentStatus.PLANNED);
        if (hasUnprocessedStudents) {
            throw new AppException(AppErrorCode.Trip.UNPROCESSED_STUDENTS,
                    messageCommon.getMessage("trip.complete.resolveStopsStudents"));
        }

        LocalDateTime completedAt = LocalDateTime.now();
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(completedAt);

        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            trip.setCompletionNote(request.getNote());
        }

        trip.markUpdated(actor(actorId));
        tripExecutionService.save(trip);

        domainNotificationService.notifyTripCompleted(trip, actorId);
        return actionResponse(trip, lastStop(stops), messageCommon.getMessage("trip.action.completed"));
    }

    @Override
    @Transactional
    public TripOperationActionResponse cancelTrip(Long tripId, CancelTripRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new AppException(AppErrorCode.Trip.ALREADY_COMPLETED, messageCommon.getMessage(AppErrorCode.Trip.ALREADY_COMPLETED));
        }
        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Trip.ALREADY_CANCELLED, messageCommon.getMessage(AppErrorCode.Trip.ALREADY_CANCELLED));
        }
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new AppException(AppErrorCode.Trip.CANCEL_REASON_REQUIRED, messageCommon.getMessage(AppErrorCode.Trip.CANCEL_REASON_REQUIRED));
        }

        double progress = calculateProgress(tripId, tenantId);

        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledAt(LocalDateTime.now());
        trip.setCancelledBy(actorId);
        trip.setCancellationReason(request.getReason());
        trip.markUpdated(actor(actorId));

        // Skip non-terminal stops
        tripStopLogService.findByTrip(tripId, tenantId).forEach(stop -> {
            if (stop.getStatus() == TripStopStatus.PENDING
                    || stop.getStatus() == TripStopStatus.ARRIVED
                    || stop.getStatus() == TripStopStatus.BOARDING) {
                stop.setStatus(TripStopStatus.SKIPPED);
                stop.setNote("Đã hủy: " + request.getReason());
                stop.markUpdated(actor(actorId));
                tripStopLogService.save(stop);
            }
        });

        // Mark PLANNED students as NOT_SERVED
        boolean isOutboundCancel = trip.getRouteDirection() == RouteDirection.OUTBOUND;
        tripStudentService.findByTrip(tripId, tenantId).stream()
                .filter(ts -> ts.getStatus() == TripStudentStatus.PLANNED)
                .forEach(ts -> {
                    ts.setStatus(TripStudentStatus.NOT_SERVED);
                    ts.setNote("Chuyến xe bị hủy: " + request.getReason());
                    ts.markUpdated(actor(actorId));
                    tripStudentService.save(ts);
                    RouteStopEntity serviceStop = isOutboundCancel ? ts.getPickupStop() : ts.getDropoffStop();
                    attendanceService.recordNotServedEvent(trip, ts, serviceStop,
                            "Chuyến xe bị hủy: " + request.getReason(), tenantId, actorId);
                });

        tripExecutionService.save(trip);

        domainNotificationService.notifyTripCancelled(trip, actorId);
        return actionResponse(trip, null, messageCommon.getMessage("trip.action.cancelled"));
    }

    @Override
    @Transactional
    public AttendanceResponse boardStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        return attendanceService.boardTripStudent(tripId, request, tenantId, actorId);
    }

    @Override
    @Transactional
    public AttendanceResponse dropoffStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        return attendanceService.dropoffTripStudent(tripId, request, tenantId, actorId);
    }

    @Override
    @Transactional
    public AttendanceResponse markStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        return attendanceService.markTripStudentAbsent(tripId, request, tenantId, actorId);
    }

    @Override
    @Transactional
    public AttendanceResponse markStudentNoShow(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        return attendanceService.markTripStudentNoShow(tripId, request, tenantId, actorId);
    }

    @Override
    @Transactional
    public AttendanceResponse markStudentNotServed(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Attendance.INVALID_STATE,
                    messageCommon.getMessage(AppErrorCode.Attendance.INVALID_STATE));
        }

        TripStudentEntity tripStudent = tripStudentService.findByTripAndStudent(tripId, request.getStudentId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.Attendance.STUDENT_NOT_IN_TRIP,
                        messageCommon.getMessage(AppErrorCode.Attendance.STUDENT_NOT_IN_TRIP)));
        RouteStopEntity routeStop = routeStopService.findRouteStop(request.getRouteStopId(), tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        messageCommon.getMessage(AppErrorCode.NOT_FOUND)));

        if (!routeStop.getRoute().getId().equals(trip.getRoute().getId())) {
            throw new AppException(AppErrorCode.Attendance.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.Attendance.INVALID_REQUEST));
        }

        TripStopLogEntity stopLog = tripStopLogService.findByTripAndRouteStop(tripId, request.getRouteStopId(), tenantId)
                .orElse(null);
        if (stopLog == null || stopLog.getStatus() != TripStopStatus.BOARDING) {
            throw new AppException(AppErrorCode.Attendance.STOP_NOT_ACTIVE,
                    messageCommon.getMessage(AppErrorCode.Attendance.STOP_NOT_ACTIVE));
        }

        if (tripStudent.getStatus() != TripStudentStatus.PLANNED) {
            throw new AppException(AppErrorCode.Attendance.STUDENT_STATUS_INVALID,
                    messageCommon.getMessage(AppErrorCode.Attendance.STUDENT_STATUS_INVALID));
        }

        tripStudent.setStatus(TripStudentStatus.NOT_SERVED);
        tripStudent.setNote(request.getNotes());
        tripStudent.markUpdated(actor(actorId));
        tripStudentService.save(tripStudent);

        attendanceService.recordNotServedEvent(trip, tripStudent, routeStop, request.getNotes(), tenantId, actorId);
        domainNotificationService.notifyAttendanceRecorded(
                trip,
                tripStudent,
                AttendanceEventType.NOT_SERVED,
                actorId);

        AttendanceResponse response = new AttendanceResponse();
        response.setRouteId(trip.getRoute() != null ? trip.getRoute().getId() : null);
        response.setRouteCode(trip.getRoute() != null ? trip.getRoute().getRouteCode() : null);
        response.setTripId(trip.getId());
        response.setRouteStopId(routeStop.getId());
        response.setStudentId(tripStudent.getStudent().getId());
        response.setStudentName(tripStudent.getStudent().getFullName());
        response.setAttendanceType(AttendanceType.CHECKED_IN.name());
        response.setEventType(AttendanceEventType.NOT_SERVED.name());
        response.setEventSource(EventSource.MANUAL.name());
        response.setStatus(AttendanceStatus.ABSENT.name());
        response.setRecordedAt(LocalDateTime.now());
        response.setRecordedBy(actorId);
        response.setNotes(request.getNotes());

        return response;
    }

    @Override
    @Transactional
    public BatchAttendanceResponse batchUpdateAttendance(Long tripId, Long routeStopId,
            BatchAttendanceRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        return attendanceService.batchUpdateAttendance(tripId, routeStopId, request, tenantId, actorId);
    }

    private int routeStopOrder(TripStopLogEntity log) {
        if (log == null || log.getRouteStop() == null || log.getRouteStop().getStopOrder() == null) {
            return Integer.MAX_VALUE;
        }
        return log.getRouteStop().getStopOrder();
    }

    private TripStopLogEntity lastStop(List<TripStopLogEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return null;
        }
        return stops.stream()
                .max(Comparator.comparingInt(this::routeStopOrder))
                .orElse(null);
    }

    private TripOperationActionResponse actionResponse(
            TripExecutionEntity trip,
            TripStopLogEntity stop,
            String message) {
        TripOperationActionResponse response = new TripOperationActionResponse();
        response.setTripId(trip == null ? null : trip.getId());
        response.setTripStatus(trip == null || trip.getStatus() == null ? null : trip.getStatus().name());
        response.setRouteStopId(stop == null || stop.getRouteStop() == null ? null : stop.getRouteStop().getId());
        response.setStopStatus(stop == null || stop.getStatus() == null ? null : stop.getStatus().name());
        response.setActualArrivalTime(stop == null ? null : stop.getActualArrivalTime());
        response.setActualDepartureTime(stop == null ? null : stop.getActualDepartureTime());
        response.setUpdatedAt(LocalDateTime.now());
        response.setMessage(message);
        return response;
    }

    private TripStopOperationProjection findOperationStop(Long tripId, Long routeStopId, Long tenantId) {
        return tripStopLogRepository.findOperationStop(tripId, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
    }

    private TripStopLogEntity findStopLogEntity(Long tripId, Long routeStopId, Long tenantId) {
        return tripStopLogRepository.findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(
                        tripId,
                        routeStopId,
                        tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
    }

    private TripStopStatus parseStopStatus(TripStopOperationProjection stop) {
        return stop == null || stop.getStopStatus() == null
                ? null
                : TripStopStatus.valueOf(stop.getStopStatus());
    }

    private RouteStopPurpose parseStopPurpose(TripStopOperationProjection stop) {
        return stop == null || stop.getStopPurpose() == null
                ? null
                : RouteStopPurpose.valueOf(stop.getStopPurpose());
    }

    private long countStudentsAtStop(Long tripId, Long routeStopId, Long tenantId, boolean outbound) {
        return outbound
                ? tripStudentRepository.countByTripAndPickupStop(tripId, routeStopId, tenantId)
                : tripStudentRepository.countByTripAndDropoffStop(tripId, routeStopId, tenantId);
    }

    private long countPendingStudentsAtStop(Long tripId, Long routeStopId, Long tenantId, boolean outbound) {
        return outbound
                ? tripStudentRepository.countPendingPickupStopStudents(tripId, routeStopId, tenantId)
                : tripStudentRepository.countPendingDropoffStopStudents(tripId, routeStopId, tenantId);
    }
}
