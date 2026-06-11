package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.message.TripOperationEventMessage;
import serp.project.school_bus_service.dto.request.CancelTripRequest;
import serp.project.school_bus_service.dto.request.CompleteTripRequest;
import serp.project.school_bus_service.dto.request.SkipStopRequest;
import serp.project.school_bus_service.dto.request.TripAttendanceActionRequest;
import serp.project.school_bus_service.dto.response.AttendanceResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.*;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.service.*;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TripOperationServiceImpl implements ITripOperationService {

    private final ITripExecutionService tripExecutionService;
    private final ITripStopLogService tripStopLogService;
    private final ITripStudentService tripStudentService;
    private final IRouteStopService routeStopService;
    private final IAttendanceService attendanceService;
    private final IAuditLogService auditLogService;
    private final ITripWebSocketPublisher webSocketPublisher;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusDataScopeService schoolBusDataScopeService;

    public TripOperationServiceImpl(
            ITripExecutionService tripExecutionService,
            ITripStopLogService tripStopLogService,
            ITripStudentService tripStudentService,
            IRouteStopService routeStopService,
            @Lazy IAttendanceService attendanceService,
            IAuditLogService auditLogService,
            ITripWebSocketPublisher webSocketPublisher,
            SchoolBusMapper mapper,
            MessageCommon messageCommon,
            ISchoolBusDataScopeService schoolBusDataScopeService) {
        this.tripExecutionService = tripExecutionService;
        this.tripStopLogService = tripStopLogService;
        this.tripStudentService = tripStudentService;
        this.routeStopService = routeStopService;
        this.attendanceService = attendanceService;
        this.auditLogService = auditLogService;
        this.webSocketPublisher = webSocketPublisher;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusDataScopeService = schoolBusDataScopeService;
    }

    private String actor(Long actorId) {
        return actorId == null ? "SYSTEM" : actorId.toString();
    }

    private TripExecutionResponse toDetail(TripExecutionEntity trip, Long tenantId) {
        return mapper.toTripExecutionResponse(
                trip,
                tripStopLogService.findByTrip(trip.getId(), tenantId),
                tripStudentService.findByTrip(trip.getId(), tenantId)
        );
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
    public TripExecutionResponse startTrip(Long tripId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.ASSIGNED && trip.getStatus() != TripStatus.PLANNED) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(trip.getId(), tenantId);
        if (stops.isEmpty()) {
            throw new AppException(AppErrorCode.Trip.NO_STOPS, messageCommon.getMessage(AppErrorCode.Trip.NO_STOPS));
        }

        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartedAt(LocalDateTime.now());
        trip.markUpdated(actor(actorId));
        tripExecutionService.save(trip);

        stops.stream()
                .min(Comparator.comparingInt(TripStopLogEntity::getStopOrder))
                .ifPresent(startTerminal -> {
                    startTerminal.setStatus(TripStopStatus.ARRIVED);
                    startTerminal.setActualArrivalTime(LocalDateTime.now());
                    startTerminal.markUpdated(actor(actorId));
                    tripStopLogService.save(startTerminal);
                });

        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "START", "Started trip");

        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setAction("START");
        msg.setTenantId(tenantId);
        msg.setEventType("TRIP_UPDATE");
        msg.setProgressPercent(0.0);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);

        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse arriveStop(Long tripId, Long routeStopId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        TripStopLogEntity stopLog = tripStopLogService.findByTripAndRouteStop(tripId, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (stopLog.getStatus() != TripStopStatus.PENDING) {
            throw new AppException(AppErrorCode.Trip.STOP_ALREADY_DONE, messageCommon.getMessage(AppErrorCode.Trip.STOP_ALREADY_DONE));
        }

        // Ensure next pending stop
        TripStopLogEntity next = tripStopLogService.findByTrip(trip.getId(), tenantId).stream()
                .filter(stop -> stop.getStatus() == TripStopStatus.PENDING)
                .min(Comparator.comparingInt(TripStopLogEntity::getStopOrder))
                .orElseThrow(() -> new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE)));
        if (!next.getId().equals(stopLog.getId())) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        LocalDateTime now = LocalDateTime.now();
        stopLog.setStatus(TripStopStatus.ARRIVED);
        stopLog.setActualArrivalTime(now);
        stopLog.markUpdated(actor(actorId));
        tripStopLogService.save(stopLog);

        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "ARRIVE_STOP",
                "Arrived stop " + stopLog.getStopOrder());

        double progress = calculateProgress(tripId, tenantId);
        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setStopId(routeStopId);
        msg.setAction("ARRIVE");
        msg.setTenantId(tenantId);
        msg.setEventType("TRIP_UPDATE");
        msg.setProgressPercent(progress);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);

        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse startBoarding(Long tripId, Long routeStopId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        TripStopLogEntity stopLog = tripStopLogService.findByTripAndRouteStop(tripId, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));

        if (stopLog.getStatus() != TripStopStatus.ARRIVED) {
            throw new AppException(AppErrorCode.Trip.STOP_NOT_ARRIVED_BOARDING,
                    messageCommon.getMessage(AppErrorCode.Trip.STOP_NOT_ARRIVED_BOARDING));
        }

        stopLog.setStatus(TripStopStatus.BOARDING);
        stopLog.markUpdated(actor(actorId));
        tripStopLogService.save(stopLog);

        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "START_BOARDING",
                "Started boarding at stop " + stopLog.getStopOrder());

        double progress = calculateProgress(tripId, tenantId);
        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setStopId(routeStopId);
        msg.setAction("START_BOARDING");
        msg.setTenantId(tenantId);
        msg.setEventType("TRIP_UPDATE");
        msg.setProgressPercent(progress);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);

        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse departStop(Long tripId, Long routeStopId, Long tenantId, Long actorId) {
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
        if (stopLog.getStatus() != TripStopStatus.ARRIVED && stopLog.getStatus() != TripStopStatus.BOARDING) {
            throw new AppException(AppErrorCode.Trip.STOP_NOT_ARRIVED, messageCommon.getMessage(AppErrorCode.Trip.STOP_NOT_ARRIVED));
        }

        RouteStopEntity routeStop = stopLog.getRouteStop();
        boolean isTerminal = (routeStop != null && routeStop.getStopPurpose() != null && routeStop.getStopPurpose().isTerminal());

        if (!isTerminal) {
            List<TripStudentEntity> allStudents = tripStudentService.findByTrip(tripId, tenantId);
            boolean isOutbound = (trip.getRouteDirection() == RouteDirection.OUTBOUND);

            List<TripStudentEntity> stopStudents = allStudents.stream()
                    .filter(ts -> {
                        if (isOutbound) {
                            return ts.getPickupStop() != null && ts.getPickupStop().getId().equals(routeStopId);
                        } else {
                            return ts.getDropoffStop() != null && ts.getDropoffStop().getId().equals(routeStopId);
                        }
                    })
                    .toList();

            if (!stopStudents.isEmpty()) {
                if (stopLog.getStatus() != TripStopStatus.BOARDING) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE, "Start boarding/dropoff at this stop before departing.");
                }

                long pendingCount = stopStudents.stream()
                        .filter(ts -> ts.getStatus() == TripStudentStatus.PLANNED)
                        .count();
                if (pendingCount > 0) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                            "Cannot depart stop because " + pendingCount + " student" + (pendingCount > 1 ? "s have" : " has") + " not been checked yet.");
                }
            }
        }

        stopLog.setStatus(TripStopStatus.DEPARTED);
        stopLog.setActualDepartureTime(LocalDateTime.now());
        stopLog.markUpdated(actor(actorId));
        tripStopLogService.save(stopLog);

        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "DEPART_STOP",
                "Departed stop " + stopLog.getStopOrder());

        double progress = calculateProgress(tripId, tenantId);
        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setStopId(routeStopId);
        msg.setAction("DEPART");
        msg.setTenantId(tenantId);
        msg.setEventType("TRIP_UPDATE");
        msg.setProgressPercent(progress);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);

        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse skipStop(Long tripId, Long routeStopId, SkipStopRequest request, Long tenantId, Long actorId) {
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
        tripStudentService.findByTrip(tripId, tenantId).stream()
                .filter(ts -> ts.getStatus() == TripStudentStatus.PLANNED)
                .filter(ts -> {
                    if (isOutbound) {
                        return ts.getPickupStop() != null && ts.getPickupStop().getId().equals(routeStopId);
                    } else {
                        return ts.getDropoffStop() != null && ts.getDropoffStop().getId().equals(routeStopId);
                    }
                })
                .forEach(ts -> {
                    ts.setStatus(TripStudentStatus.NOT_SERVED);
                    ts.setNote("Stop skipped: " + request.getReason());
                    ts.markUpdated(actor(actorId));
                    tripStudentService.save(ts);
                    attendanceService.recordNotServedEvent(trip, ts, stopLog.getRouteStop(),
                            "Stop skipped: " + request.getReason(), tenantId, actorId);
                });

        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "SKIP_STOP",
                "Skipped stop " + stopLog.getStopOrder() + ": " + request.getReason());

        double progress = calculateProgress(tripId, tenantId);
        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setStopId(routeStopId);
        msg.setAction("SKIP");
        msg.setTenantId(tenantId);
        msg.setEventType("TRIP_UPDATE");
        msg.setProgressPercent(progress);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);

        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse completeTrip(Long tripId, CompleteTripRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(tripId);
        TripExecutionEntity trip = tripExecutionService.getTripEntity(tripId, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        // All stops except the last stop (end terminal) must be DEPARTED or SKIPPED.
        // The last stop (end terminal) must be ARRIVED or DEPARTED.
        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(tripId, tenantId).stream()
                .sorted(Comparator.comparingInt(TripStopLogEntity::getStopOrder))
                .toList();

        for (int i = 0; i < stops.size(); i++) {
            TripStopLogEntity stop = stops.get(i);
            boolean isEndTerminal = (i == stops.size() - 1);
            if (isEndTerminal) {
                if (stop.getStatus() != TripStopStatus.ARRIVED && stop.getStatus() != TripStopStatus.DEPARTED) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE, "Complete all stops and resolve all student attendance before completing this trip.");
                }
            } else {
                if (stop.getStatus() != TripStopStatus.DEPARTED && stop.getStatus() != TripStopStatus.SKIPPED) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE, "Complete all stops and resolve all student attendance before completing this trip.");
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
                    ts.setNote("Auto NOT_SERVED: service stop was skipped.");
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
            throw new AppException(AppErrorCode.Trip.UNPROCESSED_STUDENTS, "Complete all stops and resolve all student attendance before completing this trip.");
        }

        LocalDateTime completedAt = LocalDateTime.now();
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(completedAt);

        if (trip.getStartedAt() != null) {
            trip.setActualDurationMin((int) Duration.between(trip.getStartedAt(), completedAt).toMinutes());
        }
        if (trip.getActualDistanceKm() == null && trip.getPlannedDistanceKm() != null) {
            trip.setActualDistanceKm(trip.getPlannedDistanceKm());
        }
        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            trip.setCompletionNote(request.getNote());
        }

        trip.markUpdated(actor(actorId));
        tripExecutionService.save(trip);

        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "COMPLETE", "Completed trip");

        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setAction("COMPLETE");
        msg.setTenantId(tenantId);
        msg.setEventType("TRIP_UPDATE");
        msg.setProgressPercent(100.0);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);

        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse cancelTrip(Long tripId, CancelTripRequest request, Long tenantId, Long actorId) {
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
                stop.setNote("Cancelled: " + request.getReason());
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
                    ts.setNote("Trip cancelled: " + request.getReason());
                    ts.markUpdated(actor(actorId));
                    tripStudentService.save(ts);
                    RouteStopEntity serviceStop = isOutboundCancel ? ts.getPickupStop() : ts.getDropoffStop();
                    attendanceService.recordNotServedEvent(trip, ts, serviceStop,
                            "Trip cancelled: " + request.getReason(), tenantId, actorId);
                });

        tripExecutionService.save(trip);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "CANCEL",
                "Cancelled trip: " + request.getReason());

        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setAction("CANCEL");
        msg.setTenantId(tenantId);
        msg.setEventType("TRIP_UPDATE");
        msg.setProgressPercent(progress);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);

        return toDetail(trip, tenantId);
    }

    private AttendanceResponse publishAttendanceEvent(Long tripId, Long routeStopId, Long studentId, String action, Long tenantId, AttendanceResponse response) {
        double progress = calculateProgress(tripId, tenantId);
        TripOperationEventMessage msg = new TripOperationEventMessage();
        msg.setTripId(tripId);
        msg.setStopId(routeStopId);
        msg.setStudentId(studentId);
        msg.setAction(action);
        msg.setTenantId(tenantId);
        msg.setEventType("ATTENDANCE_UPDATE");
        msg.setProgressPercent(progress);
        msg.setTimestamp(LocalDateTime.now());
        webSocketPublisher.publish(msg);
        return response;
    }

    @Override
    @Transactional
    public AttendanceResponse boardStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        AttendanceResponse res = attendanceService.boardTripStudent(tripId, request, tenantId, actorId);
        return publishAttendanceEvent(tripId, request.getRouteStopId(), request.getStudentId(), "BOARDED", tenantId, res);
    }

    @Override
    @Transactional
    public AttendanceResponse dropoffStudent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        AttendanceResponse res = attendanceService.dropoffTripStudent(tripId, request, tenantId, actorId);
        return publishAttendanceEvent(tripId, request.getRouteStopId(), request.getStudentId(), "DROPPED_OFF", tenantId, res);
    }

    @Override
    @Transactional
    public AttendanceResponse markStudentAbsent(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        AttendanceResponse res = attendanceService.markTripStudentAbsent(tripId, request, tenantId, actorId);
        return publishAttendanceEvent(tripId, request.getRouteStopId(), request.getStudentId(), "ABSENT", tenantId, res);
    }

    @Override
    @Transactional
    public AttendanceResponse markStudentNoShow(Long tripId, TripAttendanceActionRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanMarkAttendance(tripId);
        AttendanceResponse res = attendanceService.markTripStudentNoShow(tripId, request, tenantId, actorId);
        return publishAttendanceEvent(tripId, request.getRouteStopId(), request.getStudentId(), "NO_SHOW", tenantId, res);
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
                    "Start boarding/dropoff at this stop before marking attendance.");
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

        auditLogService.log(tenantId, actorId, "TripAttendance", null, "NOT_SERVED", "Recorded student not served manually");

        return publishAttendanceEvent(tripId, request.getRouteStopId(), request.getStudentId(), "NOT_SERVED", tenantId, response);
    }
}
