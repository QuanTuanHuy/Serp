package serp.project.school_bus_service.service.impl;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.TripExecutionParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
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
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.TripStopStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.service.IAuditLogService;
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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        trip.setStatus(TripStatus.IN_PROGRESS);
        trip.setStartedAt(LocalDateTime.now());
        trip.markUpdated(actor(actorId));
        trip.getRoute().setStatus(RouteStatus.IN_PROGRESS);
        tripRepository.save(trip);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "START", "Started trip");
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse arriveStop(Long id, Long routeStopId, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        TripStopLogEntity stop = tripStopLogService
                .findByTripAndRouteStop(id, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        ensureNextStop(trip, stop, tenantId);
        stop.setStatus(TripStopStatus.ARRIVED);
        stop.setActualArrivalTime(LocalDateTime.now());
        stop.markUpdated(actor(actorId));
        tripStopLogService.save(stop);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "ARRIVE_STOP",
                "Arrived stop " + stop.getStopOrder());
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse departStop(Long id, Long routeStopId, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        TripStopLogEntity stop = tripStopLogService
                .findByTripAndRouteStop(id, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        if (stop.getStatus() != TripStopStatus.ARRIVED && stop.getStatus() != TripStopStatus.BOARDING) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }
        stop.setStatus(TripStopStatus.DEPARTED);
        stop.setActualDepartureTime(LocalDateTime.now());
        stop.markUpdated(actor(actorId));
        tripStopLogService.save(stop);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "DEPART_STOP",
                "Departed stop " + stop.getStopOrder());
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse completeTrip(Long id, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        boolean hasPendingStops = tripStopLogService
                .findByTrip(id, tenantId)
                .stream()
                .anyMatch(stop -> stop.getStatus() != TripStopStatus.DEPARTED
                        && stop.getStatus() != TripStopStatus.SKIPPED);
        if (hasPendingStops) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(LocalDateTime.now());
        trip.markUpdated(actor(actorId));
        trip.getRoute().setStatus(RouteStatus.COMPLETED);
        tripRepository.save(trip);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "COMPLETE", "Completed trip");
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
}
