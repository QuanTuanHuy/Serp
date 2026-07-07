package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.TripExecutionParamsRequest;
import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.dto.request.CancelTripRequest;
import serp.project.school_bus_service.dto.request.CompleteTripRequest;
import serp.project.school_bus_service.dto.request.SkipStopRequest;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.TripExecutionListItemResponse;
import serp.project.school_bus_service.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.dto.response.TripListSummaryResponse;
import serp.project.school_bus_service.dto.response.TripStopLogResponse;
import serp.project.school_bus_service.dto.response.TripStudentResponse;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStopLogEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.TripStopStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.repository.projection.RouteAssignmentSummaryProjection;
import serp.project.school_bus_service.repository.projection.TripStopProgressRowProjection;
import serp.project.school_bus_service.repository.TripExecutionRepository;
import serp.project.school_bus_service.service.ISchoolBusDataScopeService;
import serp.project.school_bus_service.service.ISchoolBusDomainNotificationService;
import serp.project.school_bus_service.service.IAttendanceService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ITripExecutionService;
import serp.project.school_bus_service.service.ITripStopLogService;
import serp.project.school_bus_service.service.ITripStudentService;
import serp.project.school_bus_service.shared.auth.SchoolBusSecurityService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
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
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final ITripStopLogService tripStopLogService;
    private final ITripStudentService tripStudentService;
    private final IRouteService routeService;
    private final IRouteStopService routeStopService;
    private final IRouteDispatchService routeDispatchService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final IAttendanceService attendanceService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final ISchoolBusDataScopeService schoolBusDataScopeService;
    private final SchoolBusSecurityService securityService;
    private final ISchoolBusDomainNotificationService domainNotificationService;


    public TripExecutionServiceImpl(TripExecutionRepository tripRepository,
                                     RouteAssignmentRepository routeAssignmentRepository,
                                     ITripStopLogService tripStopLogService,
                                     ITripStudentService tripStudentService,
                                     IRouteService routeService,
                                     IRouteStopService routeStopService,
                                     IRouteDispatchService routeDispatchService,
                                     IRoutePlanStudentService routePlanStudentService,
                                     @Lazy IAttendanceService attendanceService,
                                     ICodeGeneratorService codeGeneratorService,
                                     SchoolBusMapper mapper,
                                     MessageCommon messageCommon,
                                     ISchoolBusDataScopeService schoolBusDataScopeService,
                                     SchoolBusSecurityService securityService,
                                     ISchoolBusDomainNotificationService domainNotificationService) {
        this.tripRepository = tripRepository;
        this.routeAssignmentRepository = routeAssignmentRepository;
        this.tripStopLogService = tripStopLogService;
        this.tripStudentService = tripStudentService;
        this.routeService = routeService;
        this.routeStopService = routeStopService;
        this.routeDispatchService = routeDispatchService;
        this.routePlanStudentService = routePlanStudentService;
        this.attendanceService = attendanceService;
        this.codeGeneratorService = codeGeneratorService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.schoolBusDataScopeService = schoolBusDataScopeService;
        this.securityService = securityService;
        this.domainNotificationService = domainNotificationService;
    }


    @Override
    protected BaseRepository<TripExecutionEntity, Long> getRepository() {
        return tripRepository;
    }

    @Override
    public PageResponse<TripExecutionListItemResponse> getTrips(TripExecutionParamsRequest params, Long tenantId) {
        boolean tenantWide = securityService.isAdminOrDispatcher();
        Long driverProfileId = null;
        Long attendantProfileId = null;
        Long parentProfileId = null;
        if (!tenantWide) {
            if (securityService.isDriver()) {
                driverProfileId = schoolBusDataScopeService.getCurrentDriverProfileIdRequired();
            } else if (securityService.isAttendant()) {
                attendantProfileId = schoolBusDataScopeService.getCurrentAttendantProfileIdRequired();
            } else if (securityService.isParentOnly()) {
                parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            }
        }

        Page<TripExecutionListItemResponse> tripPage = tripRepository.findTripListItems(
                tenantId,
                params == null ? null : params.getRouteId(),
                params == null ? null : params.getSchoolId(),
                params == null ? null : params.getServiceDate(),
                RouteDirection.parseNullable(params == null ? null : params.getDirection()),
                parseTripStatus(params == null ? null : params.getStatus()),
                keywordPattern(params == null ? null : params.getKeyword()),
                tenantWide,
                driverProfileId,
                attendantProfileId,
                parentProfileId,
                pageable(params, Set.of(
                        "id",
                        "tripCode",
                        "status",
                        "createdAt",
                        "updatedAt",
                        "route.routeCode",
                        "route.planningSession.serviceDate",
                        "route.planningSession.routeDirection"),
                        "createdAt"));

        List<TripExecutionListItemResponse> trips = tripPage.getContent();
        if (trips.isEmpty()) {
            return PageResponse.from(tripPage, trip -> trip);
        }

        applyTripListAssignments(trips, tenantId);
        applyTripListProgress(trips, tenantId);
        return PageResponse.from(tripPage, trip -> trip);
    }

    @Override
    @Transactional(readOnly = true)
    public TripListSummaryResponse getSummary(Long tenantId) {
        boolean tenantWide = securityService.isAdminOrDispatcher();
        Long driverProfileId = null;
        Long attendantProfileId = null;
        Long parentProfileId = null;
        if (!tenantWide) {
            if (securityService.isDriver()) {
                driverProfileId = schoolBusDataScopeService.getCurrentDriverProfileIdRequired();
            } else if (securityService.isAttendant()) {
                attendantProfileId = schoolBusDataScopeService.getCurrentAttendantProfileIdRequired();
            } else if (securityService.isParentOnly()) {
                parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            }
        }

        var summary = tripRepository.getTripListSummary(
                tenantId, tenantWide, driverProfileId, attendantProfileId, parentProfileId);
        return new TripListSummaryResponse(
                value(summary.getTotalTrips()),
                value(summary.getInProgressTrips()),
                value(summary.getCompletedTrips()));
    }

    @Override
    public TripExecutionResponse getTrip(Long id, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessTrip(id);
        TripExecutionEntity trip = findById(id, tenantId);
        return toDetail(trip, tenantId);
    }

    @Override
    public TripExecutionEntity getTripEntity(Long id, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessTrip(id);
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
        trip.setStatus(TripStatus.ASSIGNED);
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

        domainNotificationService.notifyTripCreated(saved, actorId);
        return toDetail(saved, tenantId);
    }

    // ── Trip lifecycle ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TripExecutionResponse startTrip(Long id, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(id);
        TripExecutionEntity trip = findById(id, tenantId);
        if (trip.getStatus() != TripStatus.ASSIGNED && trip.getStatus() != TripStatus.PLANNED) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }
        // Ensure at least one stop was snapshotted before starting
        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(trip.getId(), tenantId);
        stops.forEach(stop -> {
            if (stop.getRouteStop() != null) {
                routeStopService.hydrateLocation(stop.getRouteStop(), tenantId);
            }
        });
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
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse arriveStop(Long id, Long routeStopId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(id);
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
        stop.markUpdated(actor(actorId));
        tripStopLogService.save(stop);
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse departStop(Long id, Long routeStopId, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(id);
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
        // Ensure next active/current stop
        TripStopLogEntity firstUnfinished = tripStopLogService.findByTrip(id, tenantId).stream()
                .filter(s -> s.getStatus() != TripStopStatus.DEPARTED && s.getStatus() != TripStopStatus.SKIPPED)
                .min(Comparator.comparingInt(this::routeStopOrder))
                .orElseThrow(() -> new AppException(AppErrorCode.Trip.INVALID_STATE, "No active stops to depart."));
        if (!firstUnfinished.getId().equals(stop.getId())) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, "Cannot depart stop because there are earlier unfinished stops.");
        }

        RouteStopEntity routeStop = stop.getRouteStop();
        boolean isTerminal = (routeStop != null && routeStop.getStopPurpose() != null && routeStop.getStopPurpose().isTerminal());

        if (!isTerminal) {
            List<TripStudentEntity> allStudents = tripStudentService.findByTrip(id, tenantId);
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
                if (stop.getStatus() != TripStopStatus.BOARDING) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE, "Start boarding/dropoff at this stop before departing.");
                }

                long pendingCount = stopStudents.stream()
                        .filter(ts -> {
                            if (isOutbound) {
                                return ts.getStatus() == TripStudentStatus.PLANNED;
                            } else {
                                return ts.getStatus() == TripStudentStatus.BOARDED;
                            }
                        })
                        .count();
                if (pendingCount > 0) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE,
                            "Cannot depart stop because some planned students have not been processed.");
                }
            }
        }

        stop.setStatus(TripStopStatus.DEPARTED);
        stop.setActualDepartureTime(LocalDateTime.now());
        stop.markUpdated(actor(actorId));
        tripStopLogService.save(stop);
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse skipStop(Long id, Long routeStopId, SkipStopRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(id);
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

        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse completeTrip(Long id, CompleteTripRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(id);
        TripExecutionEntity trip = requireInProgress(id, tenantId);

        // All stops except the last stop (end terminal) must be DEPARTED or SKIPPED.
        // The last stop (end terminal) must be ARRIVED, BOARDING, or DEPARTED.
        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(id, tenantId).stream()
                .sorted(Comparator.comparingInt(this::routeStopOrder))
                .toList();

        for (int i = 0; i < stops.size(); i++) {
            TripStopLogEntity stop = stops.get(i);
            boolean isEndTerminal = (i == stops.size() - 1);
            if (isEndTerminal) {
                if (stop.getStatus() != TripStopStatus.ARRIVED && stop.getStatus() != TripStopStatus.BOARDING && stop.getStatus() != TripStopStatus.DEPARTED) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
                }
            } else {
                if (stop.getStatus() != TripStopStatus.DEPARTED && stop.getStatus() != TripStopStatus.SKIPPED) {
                    throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
                }
            }
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

        if (request != null && request.getNote() != null && !request.getNote().isBlank()) {
            trip.setCompletionNote(request.getNote());
        }

        trip.markUpdated(actor(actorId));
        // RoutePlan.status intentionally NOT mutated here.
        // RoutePlan keeps TRIP_CREATED; completion is recorded only on TripExecution.
        tripRepository.save(trip);
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse cancelTrip(Long id, CancelTripRequest request, Long tenantId, Long actorId) {
        schoolBusDataScopeService.assertCanOperateTrip(id);
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
        return toDetail(trip, tenantId);
    }

    // ── Trip queries ──────────────────────────────────────────────────────────

    @Override
    public List<TripStopLogResponse> getTripStops(Long id, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessTrip(id);
        findById(id, tenantId);
        return tripStopLogService.findByTrip(id, tenantId)
                .stream()
                .map(mapper::toTripStopLogResponse)
                .toList();
    }

    @Override
    public List<TripStudentResponse> getTripStudents(Long id, Long tenantId) {
        schoolBusDataScopeService.assertCanAccessTrip(id);
        findById(id, tenantId);
        List<TripStudentEntity> students = tripStudentService.findByTrip(id, tenantId);
        if (securityService.isParentOnly()) {
            Long parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            students = students.stream()
                    .filter(s -> s.getStudent() != null && s.getStudent().getParentProfile() != null 
                            && parentProfileId.equals(s.getStudent().getParentProfile().getId()))
                    .toList();
        }
        return students.stream()
                .map(mapper::toTripStudentResponse)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private TripExecutionResponse toDetail(TripExecutionEntity trip, Long tenantId) {
        List<TripStopLogEntity> stops = tripStopLogService.findByTrip(trip.getId(), tenantId);
        stops.forEach(stop -> {
            if (stop.getRouteStop() != null) {
                routeStopService.hydrateLocation(stop.getRouteStop(), tenantId);
            }
        });
        List<TripStudentEntity> students = tripStudentService.findByTrip(trip.getId(), tenantId);
        if (securityService.isParentOnly()) {
            Long parentProfileId = schoolBusDataScopeService.getCurrentParentProfileIdRequired();
            students = students.stream()
                    .filter(s -> s.getStudent() != null && s.getStudent().getParentProfile() != null 
                            && parentProfileId.equals(s.getStudent().getParentProfile().getId()))
                    .toList();
        }
        populateTripDerivedFields(trip, stops, tenantId);
        return mapper.toTripExecutionResponse(trip, stops, students);
    }

    private void populateTripDerivedFields(TripExecutionEntity trip, List<TripStopLogEntity> stops, Long tenantId) {
        if (trip == null || trip.getRoute() == null) {
            return;
        }
        routeDispatchService.findAssignmentEntityByRoute(trip.getRoute().getId(), tenantId)
                .ifPresent(assignment -> {
                    trip.setBus(assignment.getBus());
                    trip.setDriver(assignment.getDriver());
                    trip.setAttendant(assignment.getAttendant());
                });
        populateTripEndpoints(trip, stops);
    }

    private void populateTripEndpoints(TripExecutionEntity trip, List<TripStopLogEntity> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }
        stops.stream()
                .min(Comparator.comparingInt(this::routeStopOrder))
                .map(TripStopLogEntity::getRouteStop)
                .ifPresent(stop -> applyTripEndpoint(trip, stop, true));
        stops.stream()
                .max(Comparator.comparingInt(this::routeStopOrder))
                .map(TripStopLogEntity::getRouteStop)
                .ifPresent(stop -> applyTripEndpoint(trip, stop, false));
    }

    private Map<Long, RouteAssignmentSummaryProjection> assignmentSummariesByRoute(List<TripExecutionEntity> trips,
                                                                                    Long tenantId) {
        List<Long> routeIds = trips.stream()
                .filter(trip -> trip.getRoute() != null)
                .map(trip -> trip.getRoute().getId())
                .distinct()
                .toList();
        if (routeIds.isEmpty()) {
            return Map.of();
        }
        return routeAssignmentRepository.findCurrentSummariesByRouteIds(tenantId, routeIds)
                .stream()
                .collect(Collectors.toMap(RouteAssignmentSummaryProjection::getRouteId, java.util.function.Function.identity()));
    }

    private void applyTripListAssignments(List<TripExecutionListItemResponse> trips, Long tenantId) {
        List<Long> routeIds = trips.stream()
                .map(TripExecutionListItemResponse::getRouteId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (routeIds.isEmpty()) {
            return;
        }
        Map<Long, RouteAssignmentSummaryProjection> assignmentMap = routeAssignmentRepository
                .findCurrentSummariesByRouteIds(tenantId, routeIds)
                .stream()
                .collect(Collectors.toMap(RouteAssignmentSummaryProjection::getRouteId,
                        java.util.function.Function.identity()));
        trips.forEach(trip -> {
            RouteAssignmentSummaryProjection summary = assignmentMap.get(trip.getRouteId());
            if (summary == null) {
                return;
            }
            trip.setBusId(summary.getBusId());
            trip.setBusPlateNumber(summary.getBusPlateNumber());
            trip.setDriverId(summary.getDriverId());
            trip.setDriverName(summary.getDriverName());
            trip.setAttendantId(summary.getAttendantId());
            trip.setAttendantName(summary.getAttendantName());
        });
    }

    private void applyTripListProgress(List<TripExecutionListItemResponse> trips, Long tenantId) {
        List<Long> tripIds = trips.stream()
                .map(TripExecutionListItemResponse::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (tripIds.isEmpty()) {
            return;
        }
        Map<Long, List<TripStopProgressRowProjection>> rowsByTrip = tripStopLogService
                .findProgressRowsByTripIds(tripIds, tenantId)
                .stream()
                .collect(Collectors.groupingBy(TripStopProgressRowProjection::getTripId));
        trips.forEach(trip -> applyTripListProgress(trip, rowsByTrip.getOrDefault(trip.getId(), List.of())));
    }

    private void applyTripListProgress(TripExecutionListItemResponse trip,
                                       List<TripStopProgressRowProjection> rows) {
        if (rows.isEmpty()) {
            return;
        }
        List<TripStopProgressRowProjection> sorted = rows.stream()
                .sorted(Comparator.comparingInt(row -> row.getStopOrder() == null ? Integer.MAX_VALUE : row.getStopOrder()))
                .toList();
        trip.setTotalStops(sorted.size());
        int completedStops = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (isTripListStopCompleted(sorted.get(i).getStatus(), i == 0, i == sorted.size() - 1)) {
                completedStops++;
            }
        }
        trip.setCompletedStops(completedStops);
        TripStopProgressRowProjection first = sorted.get(0);
        TripStopProgressRowProjection last = sorted.get(sorted.size() - 1);
        trip.setStartLocationType(first.getLocationType());
        trip.setStartLocationName(first.getStopName());
        trip.setEndLocationType(last.getLocationType());
        trip.setEndLocationName(last.getStopName());
        sorted.stream()
                .filter(row -> isTripListNextStopStatus(row.getStatus()))
                .findFirst()
                .ifPresent(next -> {
                    trip.setNextRouteStopId(next.getRouteStopId());
                    trip.setNextStopName(next.getStopName());
                    trip.setNextStopStatus(next.getStatus());
                });
    }

    private boolean isTripListStopCompleted(String status, boolean first, boolean last) {
        if (status == null) {
            return false;
        }
        if (first) {
            return "DEPARTED".equals(status);
        }
        if (last) {
            return "ARRIVED".equals(status) || "DEPARTED".equals(status) || "SKIPPED".equals(status);
        }
        return "DEPARTED".equals(status) || "SKIPPED".equals(status);
    }

    private boolean isTripListNextStopStatus(String status) {
        return "PENDING".equals(status) || "ARRIVED".equals(status) || "BOARDING".equals(status);
    }

    private void applyAssignmentSummary(TripExecutionEntity trip, RouteAssignmentSummaryProjection summary) {
        if (trip == null || summary == null) {
            return;
        }
        if (summary.getBusId() != null) {
            BusEntity bus = new BusEntity();
            bus.setId(summary.getBusId());
            bus.setPlateNumber(summary.getBusPlateNumber());
            bus.setCapacity(summary.getBusCapacity());
            bus.setStatus(summary.getBusStatus());
            trip.setBus(bus);
        }
        if (summary.getDriverId() != null) {
            DriverProfileEntity driver = new DriverProfileEntity();
            driver.setId(summary.getDriverId());
            driver.setFullName(summary.getDriverName());
            trip.setDriver(driver);
        }
        if (summary.getAttendantId() != null) {
            BusAttendantProfileEntity attendant = new BusAttendantProfileEntity();
            attendant.setId(summary.getAttendantId());
            attendant.setFullName(summary.getAttendantName());
            trip.setAttendant(attendant);
        }
    }

    private void applyTripEndpoint(TripExecutionEntity trip, RouteStopEntity stop, boolean start) {
        if (stop == null || stop.getLocationType() == null) {
            return;
        }
        if (start) {
            trip.setStartLocationType(stop.getLocationType().name());
            trip.setStartSchool(stop.getSchool());
            trip.setStartDepot(stop.getDepot());
        } else {
            trip.setEndLocationType(stop.getLocationType().name());
            trip.setEndSchool(stop.getSchool());
            trip.setEndDepot(stop.getDepot());
        }
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

        // New model: each RoutePlanStudentEntity has pickupStop + dropoffStop directly
        for (RoutePlanStudentEntity ps : planStudents) {
            TripStudentEntity item = new TripStudentEntity();
            item.markCreated(tenantId, actor(actorId));
            item.setTrip(trip);
            item.setStudent(ps.getStudent());
            item.setSubscription(ps.getSubscription());
            item.setStatus(TripStudentStatus.PLANNED);
            item.setPickupStop(ps.getPickupStop() != null ? ps.getPickupStop() : null);
            item.setDropoffStop(ps.getDropoffStop() != null ? ps.getDropoffStop() : null);
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
                .min(Comparator.comparingInt(this::routeStopOrder))
                .orElseThrow(() -> new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE)));
        if (!next.getId().equals(target.getId())) {
            throw new AppException(AppErrorCode.Trip.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Trip.INVALID_STATE));
        }
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        BaseParamsRequest sortParams = params;
        Set<String> effectiveAllowedSorts = allowedSorts;
        String mappedSortBy = mapDerivedTripSort(params == null ? null : params.getSortBy());
        if (mappedSortBy != null) {
            sortParams = copyParamsWithSort(params, mappedSortBy);
            effectiveAllowedSorts = new HashSet<>(allowedSorts);
            effectiveAllowedSorts.add(mappedSortBy);
        }
        return PageableUtils.from(sortParams, effectiveAllowedSorts, defaultSortBy);
    }

    private String mapDerivedTripSort(String sortBy) {
        if ("serviceDate".equals(sortBy)) {
            return "route.planningSession.serviceDate";
        }
        if ("routeDirection".equals(sortBy)) {
            return "route.planningSession.routeDirection";
        }
        if ("routeCode".equals(sortBy)) {
            return "route.routeCode";
        }
        return null;
    }

    private BaseParamsRequest copyParamsWithSort(BaseParamsRequest source, String sortBy) {
        BaseParamsRequest copy = new BaseParamsRequest() {
        };
        if (source != null) {
            copy.setPage(source.getPage());
            copy.setSize(source.getSize());
            copy.setSortDirection(source.getSortDirection());
            copy.setKeyword(source.getKeyword());
        }
        copy.setSortBy(sortBy);
        return copy;
    }

    private TripStatus parseTripStatus(String status) {
        return status == null || status.isBlank() ? null : TripStatus.parse(status);
    }

    private String keywordPattern(String keyword) {
        return keyword == null || keyword.isBlank() ? null : "%" + keyword.trim().toLowerCase() + "%";
    }

    private int routeStopOrder(TripStopLogEntity log) {
        if (log == null || log.getRouteStop() == null || log.getRouteStop().getStopOrder() == null) {
            return Integer.MAX_VALUE;
        }
        return log.getRouteStop().getStopOrder();
    }

    @Override
    public long countByTenantAndStatus(Long tenantId, TripStatus status) {
        return tripRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, status);
    }

    @Override
    public boolean existsByRoute(Long routeId, Long tenantId) {
        return tripRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId).isPresent();
    }

    @Override
    public TripExecutionEntity save(TripExecutionEntity entity) {
        return tripRepository.save(entity);
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }
}
