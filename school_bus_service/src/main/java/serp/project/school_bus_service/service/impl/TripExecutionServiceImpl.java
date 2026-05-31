package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.TripExecutionParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.request.CancelTripRequest;
import serp.project.school_bus_service.dto.request.CompleteTripRequest;
import serp.project.school_bus_service.dto.request.SkipStopRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.dto.response.TripStopLogResponse;
import serp.project.school_bus_service.dto.response.TripStudentResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.RoutePlanStudentAction;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.TripStopStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.service.ITripStopLogService;
import serp.project.school_bus_service.service.ITripStudentService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TripExecutionServiceImpl extends AbstractBaseService<TripExecutionEntity, Long>
        implements ITripExecutionService {

    private final TripExecutionRepository tripRepository;
    private final ITripStopLogService tripStopLogService;
    private final ITripStudentService tripStudentService;
    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRouteDispatchService routeDispatchService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final IAuditLogService auditLogService;
    private final IAttendanceService attendanceService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;


    public TripExecutionServiceImpl(TripExecutionRepository tripRepository,
                                     ITripStopLogService tripStopLogService,
                                     ITripStudentService tripStudentService,
                                     IRouteService routeService,
                                     IRouteStopService routeStopService,
                                     IRouteDispatchService routeDispatchService,
                                     IRoutePlanStudentService routePlanStudentService,
                                     IAuditLogService auditLogService,
                                     @Lazy IAttendanceService attendanceService,
                                     ICodeGeneratorService codeGeneratorService,
                                     SchoolBusMapper mapper,
                                     MessageCommon messageCommon) {
        this.tripRepository = tripRepository;
        this.tripStopLogService = tripStopLogService;
        this.tripStudentService = tripStudentService;
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.routeDispatchService = routeDispatchService;
        this.routePlanStudentService = routePlanStudentService;
        this.auditLogService = auditLogService;
        this.attendanceService = attendanceService;
        this.codeGeneratorService = codeGeneratorService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<TripExecutionEntity, Long> getRepository() {
        return tripRepository;
    }

    @Override
    public PageResponse<TripExecutionResponse> getTrips(TripExecutionParamsRequest params, Long tenantId) {
        Specification<TripExecutionEntity> spec = BaseSpecification.tenantActiveWithKeyword(
                tenantId,
                params == null ? null : params.getKeyword(),
                "tripCode", "route.routeCode", "route.routeName", "status");
        if (params != null && params.getRouteId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("route").get("id"), params.getRouteId()));
        }
        if (params != null && params.getServiceDate() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("serviceDate"), params.getServiceDate()));
        }
        if (params != null && params.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), TripStatus.parse(params.getStatus())));
        }
        return PageResponse.from(tripRepository.findAll(
                spec,
                pageable(params, Set.of("id", "tripCode", "serviceDate", "status", "createdAt", "updatedAt"),
                        "serviceDate")),
                trip -> mapper.toTripExecutionResponse(trip, List.of(), List.of()));
    }

    @Override
    public TripExecutionResponse getTrip(Long id, Long tenantId) {
        TripExecutionEntity trip = findById(id, tenantId);
        return toDetail(trip, tenantId);
    }

    @Override
    public TripExecutionEntity getTripEntity(Long id, Long tenantId) {
        return findById(id, tenantId);
    }

    // ── Trip creation from ASSIGNED route (Phase 4 — snapshot lock) ──────────

    @Override
    @Transactional
    public TripExecutionResponse createTripFromRoute(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);

        // Only ASSIGNED routes can have trips created
        if (route.getStatus() != RouteStatus.ASSIGNED) {
            throw new AppException(AppErrorCode.Trip.ROUTE_STATUS_INVALID,
                    messageCommon.getMessage(AppErrorCode.Trip.ROUTE_STATUS_INVALID, route.getStatus()));
        }

        RouteAssignmentEntity assignment = routeDispatchService
                .findAssignmentEntityByRoute(routeId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.Trip.NO_ASSIGNMENT,
                        messageCommon.getMessage(AppErrorCode.Trip.NO_ASSIGNMENT)));

        // Check if a trip already exists for this route (idempotent)
        TripExecutionEntity trip = tripRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseGet(TripExecutionEntity::new);

        boolean isNew = trip.getId() == null;
        if (isNew) {
            trip.markCreated(tenantId, actor(actorId));
            trip.setTripCode(codeGeneratorService.generate(SchoolBusCode.TRIP.sequenceKey(),
                    SchoolBusCode.TRIP.prefix(), tenantId, actorId));
        } else {
            trip.markUpdated(actor(actorId));
        }

        // Snapshot route fields into trip
        trip.setRoute(route);
        trip.setServiceDate(route.getServiceDate());
        trip.setRouteDirection(route.getRouteDirection());
        trip.setShiftType(route.getShiftType());
        trip.setStatus(TripStatus.ASSIGNED);
        trip.setPlannedDistanceKm(route.getPlannedDistanceKm());
        trip.setPlannedDurationMin(route.getPlannedDurationMin());
        trip.setRouteGeometryPath(route.getGeometryPath());

        // Snapshot assignment resources into trip
        trip.setBus(assignment.getBus());
        trip.setDriver(assignment.getDriver());
        trip.setAttendant(assignment.getAttendant());

        // Snapshot start/end locations from route
        trip.setStartLocationType(route.getStartLocationType() == null ? null : route.getStartLocationType().name());
        trip.setStartSchool(route.getStartSchool());
        trip.setStartDepot(route.getStartDepot());
        trip.setEndLocationType(route.getEndLocationType() == null ? null : route.getEndLocationType().name());
        trip.setEndSchool(route.getEndSchool());
        trip.setEndDepot(route.getEndDepot());

        TripExecutionEntity saved = tripRepository.save(trip);

        // Snapshot stops and students from route plan (NOT dynamically from subscriptions)
        snapshotStops(saved, route, tenantId, actorId);
        snapshotStudentsFromRoutePlan(saved, route, tenantId, actorId);

        // Transition route status to TRIP_CREATED
        route.setStatus(RouteStatus.TRIP_CREATED);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);

        auditLogService.log(tenantId, actorId, "TripExecution", saved.getId(), "CREATE_FROM_ROUTE",
                "Created trip execution from route plan (snapshot locked)");
        return toDetail(saved, tenantId);
    }

    // ── Trip lifecycle ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TripExecutionResponse startTrip(Long id, Long tenantId, Long actorId) {
        TripExecutionEntity trip = findById(id, tenantId);
        if (trip.getStatus() != TripStatus.ASSIGNED && trip.getStatus() != TripStatus.PLANNED) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }
        // Ensure at least one stop was snapshotted before starting
        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(trip.getId(), tenantId);
        if (stops.isEmpty()) {
            throw new AppException(AppErrorCode.Trip.NO_STOPS, messageCommon.getMessage(AppErrorCode.Trip.NO_STOPS));
        }
        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartedAt(LocalDateTime.now());
        trip.markUpdated(actor(actorId));
        // RoutePlan.status intentionally NOT mutated here.
        // RoutePlan keeps TRIP_CREATED throughout trip execution.
        // Operational state is tracked exclusively by TripExecution.status.
        tripRepository.save(trip);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "START", "Started trip");
        // TODO notification: notify parents/admins when trip starts.
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse arriveStop(Long id, Long routeStopId, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        TripStopLogEntity stop = tripStopLogService
                .findByTripAndRouteStop(id, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        // Guard: only PENDING stops can be arrived at
        if (stop.getStatus() != TripStopStatus.PENDING) {
            throw new AppException(AppErrorCode.Trip.STOP_ALREADY_DONE, messageCommon.getMessage(AppErrorCode.Trip.STOP_ALREADY_DONE));
        }
        ensureNextStop(trip, stop, tenantId);
        LocalDateTime now = LocalDateTime.now();
        stop.setStatus(TripStopStatus.ARRIVED);
        stop.setActualArrivalTime(now);
        // Calculate delay: compare actual arrival vs planned arrival time on service date
        LocalTime plannedArrivalTime = stop.getRouteStop().getPlannedArrivalTime();
        if (plannedArrivalTime != null) {
            LocalDateTime planned = LocalDateTime.of(
                    trip.getServiceDate() != null ? trip.getServiceDate() : LocalDate.now(),
                    plannedArrivalTime);
            long delayMin = Duration.between(planned, now).toMinutes();
            stop.setDelayMinutes((int) delayMin);
        }
        stop.markUpdated(actor(actorId));
        tripStopLogService.save(stop);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "ARRIVE_STOP",
                "Arrived stop " + stop.getStopOrder());
        // TODO notification: notify parents/guardians when bus arrives at this stop.
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse departStop(Long id, Long routeStopId, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        TripStopLogEntity stop = tripStopLogService
                .findByTripAndRouteStop(id, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        // Guard: can only depart an ARRIVED or BOARDING stop
        if (stop.getStatus() == TripStopStatus.DEPARTED || stop.getStatus() == TripStopStatus.SKIPPED) {
            throw new AppException(AppErrorCode.Trip.STOP_ALREADY_DONE, messageCommon.getMessage(AppErrorCode.Trip.STOP_ALREADY_DONE));
        }
        if (stop.getStatus() != TripStopStatus.ARRIVED && stop.getStatus() != TripStopStatus.BOARDING) {
            throw new AppException(AppErrorCode.Trip.STOP_NOT_ARRIVED, messageCommon.getMessage(AppErrorCode.Trip.STOP_NOT_ARRIVED));
        }
        stop.setStatus(TripStopStatus.DEPARTED);
        stop.setActualDepartureTime(LocalDateTime.now());
        stop.markUpdated(actor(actorId));
        tripStopLogService.save(stop);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "DEPART_STOP",
                "Departed stop " + stop.getStopOrder());
        // TODO notification: notify parents/guardians when bus departs this stop.
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse skipStop(Long id, Long routeStopId, SkipStopRequest request, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        TripStopLogEntity stop = tripStopLogService
                .findByTripAndRouteStop(id, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        // Can skip a PENDING, ARRIVED, or BOARDING stop.
        // Once DEPARTED or SKIPPED, the stop cannot be skipped again.
        if (stop.getStatus() == TripStopStatus.DEPARTED || stop.getStatus() == TripStopStatus.SKIPPED) {
            throw new AppException(AppErrorCode.Trip.STOP_ALREADY_DONE, messageCommon.getMessage(AppErrorCode.Trip.STOP_ALREADY_DONE));
        }
        // Terminal stops (DEPOT / SCHOOL terminals) must not be skipped
        RouteStopPurpose purpose = stop.getRouteStop() != null ? stop.getRouteStop().getStopPurpose() : null;
        if (purpose != null && purpose.isTerminal()) {
            throw new AppException(AppErrorCode.Trip.CANNOT_SKIP_TERMINAL,
                    messageCommon.getMessage(AppErrorCode.Trip.CANNOT_SKIP_TERMINAL));
        }
        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new AppException(AppErrorCode.Trip.SKIP_REASON_REQUIRED, messageCommon.getMessage(AppErrorCode.Trip.SKIP_REASON_REQUIRED));
        }
        stop.setStatus(TripStopStatus.SKIPPED);
        stop.setNote(request.getReason());
        stop.markUpdated(actor(actorId));
        tripStopLogService.save(stop);

        // Immediately mark PLANNED students whose service stop is this stop as NOT_SERVED.
        // NOT_SERVED = stop was skipped before the student could be served (operational, not the student's fault).
        // Direction-aware:
        //   OUTBOUND: the critical stop is pickupStop (home → school; student boards here).
        //   RETURN:   the critical stop is dropoffStop (school → home; student alights here).
        boolean isOutbound = trip.getRouteDirection() == RouteDirection.OUTBOUND;
        tripStudentService.findByTrip(id, tenantId).stream()
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
                    // Log NOT_SERVED attendance event for audit trail
                    attendanceService.recordNotServedEvent(trip, ts, stop.getRouteStop(),
                            "Stop skipped: " + request.getReason(), tenantId, actorId);
                });

        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "SKIP_STOP",
                "Skipped stop " + stop.getStopOrder() + ": " + request.getReason());
        // TODO notification: notify parents/guardians that this stop has been skipped.
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse completeTrip(Long id, CompleteTripRequest request, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);

        // All stops must be DEPARTED or SKIPPED
        boolean hasPendingStops = tripStopLogService
                .findByTrip(id, tenantId)
                .stream()
                .anyMatch(stop -> stop.getStatus() != TripStopStatus.DEPARTED
                        && stop.getStatus() != TripStopStatus.SKIPPED);
        if (hasPendingStops) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }

        // Auto-resolve PLANNED students whose service stop was SKIPPED as NOT_SERVED (safety net).
        // skipStop() already marks students NOT_SERVED immediately; this covers edge cases where stops
        // were skipped via cancelTrip or other paths that bypass skipStop().
        // Direction-aware: OUTBOUND checks pickupStop (home stop); RETURN checks dropoffStop (home stop).
        boolean isOutboundComplete = trip.getRouteDirection() == RouteDirection.OUTBOUND;
        Set<Long> skippedRouteStopIds = tripStopLogService.findByTrip(id, tenantId).stream()
                .filter(s -> s.getStatus() == TripStopStatus.SKIPPED)
                .map(s -> s.getRouteStop().getId())
                .collect(Collectors.toSet());
        tripStudentService.findByTrip(id, tenantId).stream()
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

        // Block if any PLANNED students remain — these are students at DEPARTED stops whom the attendant
        // did not process (board / absent / no-show). They must be resolved before the trip can complete.
        boolean hasUnprocessedStudents = tripStudentService
                .findByTrip(id, tenantId)
                .stream()
                .anyMatch(s -> s.getStatus() == TripStudentStatus.PLANNED);
        if (hasUnprocessedStudents) {
            throw new AppException(AppErrorCode.Trip.UNPROCESSED_STUDENTS, messageCommon.getMessage(AppErrorCode.Trip.UNPROCESSED_STUDENTS));
        }

        LocalDateTime completedAt = LocalDateTime.now();
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(completedAt);

        // Calculate actual duration from startedAt to completedAt
        if (trip.getStartedAt() != null) {
            trip.setActualDurationMin((int) Duration.between(trip.getStartedAt(), completedAt).toMinutes());
        }

        // Fallback: actual GPS distance not available; using planned distance.
        if (trip.getActualDistanceKm() == null && trip.getPlannedDistanceKm() != null) {
            trip.setActualDistanceKm(trip.getPlannedDistanceKm());
        }

        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            trip.setCompletionNote(request.getNote());
        }

        trip.markUpdated(actor(actorId));
        // RoutePlan.status intentionally NOT mutated here.
        // RoutePlan keeps TRIP_CREATED; completion is recorded only on TripExecution.
        tripRepository.save(trip);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "COMPLETE", "Completed trip");
        // TODO notification: notify parents/admins when trip is completed.
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse cancelTrip(Long id, CancelTripRequest request, Long tenantId, Long actorId) {
        TripExecutionEntity trip = findById(id, tenantId);
        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new AppException(AppErrorCode.Trip.ALREADY_COMPLETED, messageCommon.getMessage(AppErrorCode.Trip.ALREADY_COMPLETED));
        }
        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Trip.ALREADY_CANCELLED, messageCommon.getMessage(AppErrorCode.Trip.ALREADY_CANCELLED));
        }
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new AppException(AppErrorCode.Trip.CANCEL_REASON_REQUIRED, messageCommon.getMessage(AppErrorCode.Trip.CANCEL_REASON_REQUIRED));
        }
        trip.setStatus(TripStatus.CANCELLED);
        trip.setCancelledAt(LocalDateTime.now());
        trip.setCancelledBy(actorId);
        trip.setCancellationReason(request.getReason());
        trip.markUpdated(actor(actorId));

        // Mark all non-terminal stops (PENDING, ARRIVED, BOARDING) as SKIPPED
        tripStopLogService.findByTrip(id, tenantId).forEach(stop -> {
            if (stop.getStatus() == TripStopStatus.PENDING
                    || stop.getStatus() == TripStopStatus.ARRIVED
                    || stop.getStatus() == TripStopStatus.BOARDING) {
                stop.setStatus(TripStopStatus.SKIPPED);
                stop.setNote("Cancelled: " + request.getReason());
                stop.markUpdated(actor(actorId));
                tripStopLogService.save(stop);
            }
        });

        // Mark PLANNED students as NOT_SERVED — the trip was cancelled before they could be served.
        // BOARDED students (already on the bus) are deliberately NOT touched here:
        //   they require manual resolution (e.g. return to depot/school) outside the trip lifecycle.
        // TODO domain: introduce a RETURNED_WITHOUT_SERVICE or similar status for BOARDED students
        //   whose trip is cancelled mid-route, so their outcome can also be recorded.
        boolean isOutboundCancel = trip.getRouteDirection() == RouteDirection.OUTBOUND;
        tripStudentService.findByTrip(id, tenantId).stream()
                .filter(ts -> ts.getStatus() == TripStudentStatus.PLANNED)
                .forEach(ts -> {
                    ts.setStatus(TripStudentStatus.NOT_SERVED);
                    ts.setNote("Trip cancelled: " + request.getReason());
                    ts.markUpdated(actor(actorId));
                    tripStudentService.save(ts);
                    // Log NOT_SERVED attendance event — use service stop as routeStop context
                    serp.project.school_bus_service.entity.RouteStopEntity serviceStop =
                            isOutboundCancel ? ts.getPickupStop() : ts.getDropoffStop();
                    attendanceService.recordNotServedEvent(trip, ts, serviceStop,
                            "Trip cancelled: " + request.getReason(), tenantId, actorId);
                });

        tripRepository.save(trip);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "CANCEL",
                "Cancelled trip: " + request.getReason());
        // TODO notification: notify parents/admins when trip is cancelled.
        return toDetail(trip, tenantId);
    }

    // ── Trip queries ──────────────────────────────────────────────────────────

    @Override
    public List<TripStopLogResponse> getTripStops(Long id, Long tenantId) {
        findById(id, tenantId);
        return tripStopLogService.findByTrip(id, tenantId)
                .stream()
                .map(mapper::toTripStopLogResponse)
                .toList();
    }

    @Override
    public List<TripStudentResponse> getTripStudents(Long id, Long tenantId) {
        findById(id, tenantId);
        return tripStudentService.findByTrip(id, tenantId)
                .stream()
                .map(mapper::toTripStudentResponse)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private TripExecutionResponse toDetail(TripExecutionEntity trip, Long tenantId) {
        return mapper.toTripExecutionResponse(trip,
                tripStopLogService.findByTrip(trip.getId(),
                        tenantId),
                tripStudentService.findByTrip(trip.getId(),
                        tenantId));
    }

    /**
     * Snapshot route stops into trip stop logs (idempotent — skips if already exist).
     */
    private void snapshotStops(TripExecutionEntity trip, RoutePlanEntity route, Long tenantId, Long actorId) {
        List<TripStopLogEntity> existing = tripStopLogService
                .findByTrip(trip.getId(), tenantId);
        if (!existing.isEmpty()) {
            return;
        }
        for (RouteStopEntity routeStop : routeStopService
                .findByRoute(route.getId(), tenantId)) {
            TripStopLogEntity log = new TripStopLogEntity();
            log.markCreated(tenantId, actor(actorId));
            log.setTrip(trip);
            log.setRouteStop(routeStop);
            log.setStopOrder(routeStop.getStopOrder());
            log.setStatus(TripStopStatus.PENDING);
            tripStopLogService.save(log);
        }
    }

    /**
     * Phase 4: Snapshot students from RoutePlanStudent (planning-time snapshot)
     * instead of dynamically querying subscriptions.
     *
     * Each student may have two RoutePlanStudent entries (BOARD + DROPOFF).
     * We group them by student+subscription and map to a single TripStudent
     * with pickupStop (BOARD) and dropoffStop (DROPOFF).
     */
    private void snapshotStudentsFromRoutePlan(TripExecutionEntity trip, RoutePlanEntity route,
                                               Long tenantId, Long actorId) {
        List<TripStudentEntity> existing = tripStudentService
                .findByTrip(trip.getId(), tenantId);
        if (!existing.isEmpty()) {
            return;
        }

        List<RoutePlanStudentEntity> planStudents = routePlanStudentService
                .findByRoute(route.getId());

        // Group by (studentId, subscriptionId) → collect BOARD and DROPOFF stops
        // Key = "studentId:subscriptionId"
        Map<String, RoutePlanStudentEntity> boardMap = new HashMap<>();
        Map<String, RoutePlanStudentEntity> dropoffMap = new HashMap<>();

        for (RoutePlanStudentEntity ps : planStudents) {
            String key = ps.getStudent().getId() + ":" + ps.getSubscription().getId();
            if (ps.getServiceAction() == RoutePlanStudentAction.BOARD) {
                boardMap.put(key, ps);
            } else if (ps.getServiceAction() == RoutePlanStudentAction.DROPOFF) {
                dropoffMap.put(key, ps);
            }
        }

        // Merge keys from both maps
        Set<String> allKeys = new HashSet<>(boardMap.keySet());
        allKeys.addAll(dropoffMap.keySet());

        for (String key : allKeys) {
            RoutePlanStudentEntity boardEntry = boardMap.get(key);
            RoutePlanStudentEntity dropoffEntry = dropoffMap.get(key);

            // Use whichever entry exists to get the student and subscription references
            RoutePlanStudentEntity reference = boardEntry != null ? boardEntry : dropoffEntry;

            TripStudentEntity item = new TripStudentEntity();
            item.markCreated(tenantId, actor(actorId));
            item.setTrip(trip);
            item.setStudent(reference.getStudent());
            item.setSubscription(reference.getSubscription());
            item.setStatus(TripStudentStatus.PLANNED);
            item.setPickupStop(boardEntry != null ? boardEntry.getRouteStop() : null);
            item.setDropoffStop(dropoffEntry != null ? dropoffEntry.getRouteStop() : null);
            tripStudentService.save(item);
        }
    }

    private TripExecutionEntity requireInProgress(Long id, Long tenantId) {
        TripExecutionEntity trip = findById(id, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }
        return trip;
    }

    private void ensureNextStop(TripExecutionEntity trip, TripStopLogEntity target, Long tenantId) {
        TripStopLogEntity next = tripStopLogService
                .findByTrip(trip.getId(), tenantId)
                .stream()
                .filter(stop -> stop.getStatus() == TripStopStatus.PENDING)
                .min(Comparator.comparingInt(TripStopLogEntity::getStopOrder))
                .orElseThrow(() -> new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE)));
        if (!next.getId().equals(target.getId())) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }

    @Override
    public long countByTenantAndStatus(Long tenantId, TripStatus status) {
        return tripRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, status);
    }
}
