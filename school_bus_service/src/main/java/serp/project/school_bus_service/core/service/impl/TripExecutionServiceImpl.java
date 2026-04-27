package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.TripExecutionParamsRequest;
import serp.project.school_bus_service.application.dto.request.BaseParamsRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.TripExecutionResponse;
import serp.project.school_bus_service.application.dto.response.TripStopLogResponse;
import serp.project.school_bus_service.application.dto.response.TripStudentResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IStudentSubscriptionService;
import serp.project.school_bus_service.core.service.ITripExecutionService;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.TripStopStatus;
import serp.project.school_bus_service.enums.TripStudentStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentSubscriptionEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripExecutionEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripStopLogEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripStudentEntity;
import serp.project.school_bus_service.infrastructure.store.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RouteStopRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripExecutionRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripStopLogRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripStudentRepository;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TripExecutionServiceImpl extends AbstractBaseService<TripExecutionEntity, Long>
        implements ITripExecutionService {

    private final TripExecutionRepository tripRepository;
    private final TripStopLogRepository tripStopLogRepository;
    private final TripStudentRepository tripStudentRepository;
    private final RoutePlanRepository routePlanRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final IStudentSubscriptionService subscriptionService;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;

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
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parseTripStatus(params.getStatus())));
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

    @Override
    @Transactional
    public TripExecutionResponse createTripFromRoute(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routePlanRepository.findByIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        RouteAssignmentEntity assignment = routeAssignmentRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.INVALID_STATE, "Route must be assigned before trip creation"));
        TripExecutionEntity trip = tripRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseGet(TripExecutionEntity::new);
        if (trip.getId() == null) {
            trip.markCreated(tenantId, actor(actorId));
            trip.setTripCode(codeGeneratorService.generate(SchoolBusCode.TRIP.sequenceKey(),
                    SchoolBusCode.TRIP.prefix(), tenantId, actorId));
        } else {
            trip.markUpdated(actor(actorId));
        }
        trip.setRoute(route);
        trip.setServiceDate(route.getServiceDate());
        trip.setRouteDirection(route.getRouteDirection());
        trip.setShiftType(route.getShiftType());
        trip.setStatus(TripStatus.ASSIGNED);
        trip.setPlannedDistanceKm(route.getPlannedDistanceKm());
        trip.setPlannedDurationMin(route.getPlannedDurationMin());
        trip.setRouteGeometryPath(route.getGeometryPath());
        trip.setBus(assignment.getBus());
        trip.setDriver(assignment.getDriver());
        trip.setAttendant(assignment.getAttendant());
        TripExecutionEntity saved = tripRepository.save(trip);
        snapshotStops(saved, route, tenantId, actorId);
        snapshotStudents(saved, route, tenantId, actorId);
        auditLogService.log(tenantId, actorId, "TripExecution", saved.getId(), "CREATE_FROM_ROUTE",
                "Created trip execution from route");
        return toDetail(saved, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse startTrip(Long id, Long tenantId, Long actorId) {
        TripExecutionEntity trip = findById(id, tenantId);
        if (trip.getStatus() != TripStatus.ASSIGNED && trip.getStatus() != TripStatus.PLANNED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
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
        TripStopLogEntity stop = tripStopLogRepository
                .findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(id, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        ensureNextStop(trip, stop, tenantId);
        stop.setStatus(TripStopStatus.ARRIVED);
        stop.setActualArrivalTime(LocalDateTime.now());
        stop.markUpdated(actor(actorId));
        tripStopLogRepository.save(stop);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "ARRIVE_STOP",
                "Arrived stop " + stop.getStopOrder());
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse departStop(Long id, Long routeStopId, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        TripStopLogEntity stop = tripStopLogRepository
                .findByTripIdAndRouteStopIdAndTenantIdAndIsDeletedFalse(id, routeStopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND));
        if (stop.getStatus() != TripStopStatus.ARRIVED && stop.getStatus() != TripStopStatus.BOARDING) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }
        stop.setStatus(TripStopStatus.DEPARTED);
        stop.setActualDepartureTime(LocalDateTime.now());
        stop.markUpdated(actor(actorId));
        tripStopLogRepository.save(stop);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "DEPART_STOP",
                "Departed stop " + stop.getStopOrder());
        return toDetail(trip, tenantId);
    }

    @Override
    @Transactional
    public TripExecutionResponse completeTrip(Long id, Long tenantId, Long actorId) {
        TripExecutionEntity trip = requireInProgress(id, tenantId);
        boolean hasPendingStops = tripStopLogRepository
                .findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(id, tenantId)
                .stream()
                .anyMatch(stop -> stop.getStatus() != TripStopStatus.DEPARTED && stop.getStatus() != TripStopStatus.SKIPPED);
        if (hasPendingStops) {
            throw new AppException(AppErrorCode.INVALID_STATE, "Trip cannot complete before all stops are handled");
        }
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(LocalDateTime.now());
        trip.markUpdated(actor(actorId));
        trip.getRoute().setStatus(RouteStatus.COMPLETED);
        tripRepository.save(trip);
        auditLogService.log(tenantId, actorId, "TripExecution", trip.getId(), "COMPLETE", "Completed trip");
        return toDetail(trip, tenantId);
    }

    @Override
    public List<TripStopLogResponse> getTripStops(Long id, Long tenantId) {
        findById(id, tenantId);
        return tripStopLogRepository.findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(id, tenantId)
                .stream()
                .map(mapper::toTripStopLogResponse)
                .toList();
    }

    @Override
    public List<TripStudentResponse> getTripStudents(Long id, Long tenantId) {
        findById(id, tenantId);
        return tripStudentRepository.findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(id, tenantId)
                .stream()
                .map(mapper::toTripStudentResponse)
                .toList();
    }

    private TripExecutionResponse toDetail(TripExecutionEntity trip, Long tenantId) {
        return mapper.toTripExecutionResponse(trip,
                tripStopLogRepository.findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(trip.getId(), tenantId),
                tripStudentRepository.findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(trip.getId(),
                        tenantId));
    }

    private void snapshotStops(TripExecutionEntity trip, RoutePlanEntity route, Long tenantId, Long actorId) {
        List<TripStopLogEntity> existing = tripStopLogRepository
                .findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(trip.getId(), tenantId);
        if (!existing.isEmpty()) {
            return;
        }
        for (RouteStopEntity routeStop : routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(route.getId(), tenantId)) {
            TripStopLogEntity log = new TripStopLogEntity();
            log.markCreated(tenantId, actor(actorId));
            log.setTrip(trip);
            log.setRouteStop(routeStop);
            log.setStopOrder(routeStop.getStopOrder());
            log.setStatus(TripStopStatus.PENDING);
            tripStopLogRepository.save(log);
        }
    }

    private void snapshotStudents(TripExecutionEntity trip, RoutePlanEntity route, Long tenantId, Long actorId) {
        List<TripStudentEntity> existing = tripStudentRepository
                .findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(trip.getId(), tenantId);
        if (!existing.isEmpty()) {
            return;
        }
        List<RouteStopEntity> stops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(route.getId(), tenantId);
        for (StudentSubscriptionEntity subscription : subscriptionService.findEligibleSubscriptions(
                route.getSchool().getId(), route.getRouteDirection(), route.getServiceDate(), tenantId)) {
            TripStudentEntity item = new TripStudentEntity();
            item.markCreated(tenantId, actor(actorId));
            item.setTrip(trip);
            item.setStudent(subscription.getStudent());
            item.setSubscription(subscription);
            item.setStatus(TripStudentStatus.PLANNED);
            item.setPickupStop(matchStop(stops, subscription, route.getRouteDirection()));
            item.setDropoffStop(matchStop(stops, subscription, route.getRouteDirection()));
            tripStudentRepository.save(item);
        }
    }

    private RouteStopEntity matchStop(List<RouteStopEntity> stops, StudentSubscriptionEntity subscription,
            RouteDirection direction) {
        Long pointId = direction == RouteDirection.RETURN
                ? (subscription.getDropoffPoint() == null ? null : subscription.getDropoffPoint().getId())
                : (subscription.getPickupPoint() == null ? null : subscription.getPickupPoint().getId());
        if (pointId == null) {
            return null;
        }
        return stops.stream()
                .filter(stop -> stop.getPickupPoint().getId().equals(pointId))
                .findFirst()
                .orElse(null);
    }

    private TripExecutionEntity requireInProgress(Long id, Long tenantId) {
        TripExecutionEntity trip = findById(id, tenantId);
        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }
        return trip;
    }

    private void ensureNextStop(TripExecutionEntity trip, TripStopLogEntity target, Long tenantId) {
        TripStopLogEntity next = tripStopLogRepository
                .findByTripIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(trip.getId(), tenantId)
                .stream()
                .filter(stop -> stop.getStatus() == TripStopStatus.PENDING)
                .min(Comparator.comparingInt(TripStopLogEntity::getStopOrder))
                .orElseThrow(() -> new AppException(AppErrorCode.INVALID_STATE));
        if (!next.getId().equals(target.getId())) {
            throw new AppException(AppErrorCode.INVALID_STATE, "Only the next pending stop can be arrived");
        }
    }

    private TripStatus parseTripStatus(String value) {
        try {
            return TripStatus.valueOf(value == null ? "" : value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Invalid trip status: " + value);
        }
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }
}

