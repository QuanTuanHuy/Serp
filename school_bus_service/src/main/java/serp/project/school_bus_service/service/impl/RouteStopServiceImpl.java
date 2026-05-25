package serp.project.school_bus_service.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import serp.project.school_bus_service.dto.request.AddRouteStopRequest;
import serp.project.school_bus_service.dto.request.AddStudentToStopRequest;
import serp.project.school_bus_service.dto.request.MoveStudentRequest;
import serp.project.school_bus_service.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.entity.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RoutePlanStudentAction;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.RouteStopType;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.repository.RouteStopRepository;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RouteStopServiceImpl extends AbstractBaseService<RouteStopEntity, Long>
        implements IRouteStopService {

    private final RouteStopRepository routeStopRepository;
    private final IRouteService routeService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final IPickupPointService pickupPointService;
    private final IRouteGeometryService routeGeometryService;
    private final IAuditLogService auditLogService;
    private final IRoutePlanningSessionService planningSessionService;
    private final IStudentSubscriptionService subscriptionService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;

    public RouteStopServiceImpl(RouteStopRepository routeStopRepository,
                                @Lazy IRouteService routeService,
                                IRoutePlanStudentService routePlanStudentService,
                                IPickupPointService pickupPointService,
                                IRouteGeometryService routeGeometryService,
                                IAuditLogService auditLogService,
                                @Lazy IRoutePlanningSessionService planningSessionService,
                                IStudentSubscriptionService subscriptionService,
                                SchoolBusMapper mapper,
                                MessageCommon messageCommon) {
        this.routeStopRepository = routeStopRepository;
        this.routeService = routeService;
        this.routePlanStudentService = routePlanStudentService;
        this.pickupPointService = pickupPointService;
        this.routeGeometryService = routeGeometryService;
        this.auditLogService = auditLogService;
        this.planningSessionService = planningSessionService;
        this.subscriptionService = subscriptionService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<RouteStopEntity, Long> getRepository() {
        return routeStopRepository;
    }

    // ── Reorder ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId,
            Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);
        requireSessionEditable(route);

        List<RouteStopEntity> stops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);
        if (stops.size() != request.getOrderedStopIds().size()) {
            throw new AppException(AppErrorCode.RouteStop.COUNT_MISMATCH, messageCommon.getMessage(AppErrorCode.RouteStop.COUNT_MISMATCH));
        }

        for (int i = 0; i < request.getOrderedStopIds().size(); i++) {
            Long stopId = request.getOrderedStopIds().get(i);
            RouteStopEntity stop = stops.stream().filter(s -> s.getId().equals(stopId)).findFirst()
                    .orElseThrow(() -> new AppException(AppErrorCode.RouteStop.NOT_FOUND, messageCommon.getMessage(AppErrorCode.RouteStop.NOT_FOUND)));
            stop.setStopOrder(i);
        }

        routeStopRepository.saveAll(stops);
        recalculateGeometry(route, tenantId);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "REORDER_STOPS", "Reordered route stops");
        return routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId)
                .stream()
                .map(mapper::toRouteStopResponse)
                .toList();
    }

    // ── Add stop ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RouteStopResponse addStop(Long routeId, AddRouteStopRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);
        requireSessionEditable(route);

        PickupPointEntity pickupPoint = pickupPointService.getPickupPoint(request.getPickupPointId(), tenantId);

        List<RouteStopEntity> existingStops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);

        // Check for duplicate pickup point on same route
        boolean duplicate = existingStops.stream()
                .anyMatch(s -> s.getPickupPoint().getId().equals(request.getPickupPointId()));
        if (duplicate) {
            throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.RouteStop.INVALID_REQUEST));
        }

        RouteStopType stopType = RouteStopType.PICKUP;
        if (request.getStopType() != null) {
            try {
                stopType = RouteStopType.valueOf(request.getStopType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(AppErrorCode.Route.FIELD_INVALID, messageCommon.getMessage(AppErrorCode.Route.FIELD_INVALID, "stop type", request.getStopType()));
            }
        }

        RouteStopEntity stop = new RouteStopEntity();
        stop.markCreated(tenantId, actor(actorId));
        stop.setRoute(route);
        stop.setPickupPoint(pickupPoint);
        stop.setStopType(stopType);
        stop.setStopOrder(existingStops.size()); // append at end
        stop.setEstimatedStudentCount(request.getEstimatedStudentCount() != null ? request.getEstimatedStudentCount() : 0);
        stop.setPlannedBoardingCount(0);
        stop.setPlannedDropoffCount(0);

        RouteStopEntity saved = routeStopRepository.save(stop);

        // Update route geometry and metadata
        recalculateGeometry(route, tenantId);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);

        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "ADD_STOP",
                "Added stop: " + pickupPoint.getName());
        return mapper.toRouteStopResponse(saved);
    }

    // ── Assign student to route (auto-create stop if needed) ─────────────────

    @Override
    @Transactional
    public RoutePlanStudentResponse assignStudentToRoute(Long routeId,
                                                         AddStudentToStopRequest request,
                                                         Long tenantId, Long actorId) {
        // 1. Load and validate route
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);

        // 2. Route must belong to a planning session
        RoutePlanningSessionEntity session = route.getPlanningSession();
        if (session == null) {
            throw new AppException(AppErrorCode.RouteStop.SESSION_NOT_LINKED,
                    messageCommon.getMessage(AppErrorCode.RouteStop.SESSION_NOT_LINKED));
        }
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED
                || session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.RouteStop.SESSION_FROZEN,
                    messageCommon.getMessage(AppErrorCode.RouteStop.SESSION_FROZEN, session.getStatus()));
        }

        // 3. Load and validate subscription
        StudentSubscriptionEntity subscription = subscriptionService.getSubscriptionEntity(
                request.getSubscriptionId(), tenantId);
        StudentEntity student = subscription.getStudent();
        if (!student.getId().equals(request.getStudentId())) {
            throw new AppException(AppErrorCode.RouteStop.STUDENT_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_MISMATCH));
        }

        // 4. Determine relevant point for this direction
        RouteDirection direction = route.getRouteDirection();
        PickupPointEntity relevantPoint = direction == RouteDirection.OUTBOUND
                ? subscription.getPickupPoint()
                : subscription.getDropoffPoint();
        if (relevantPoint == null) {
            throw new AppException(AppErrorCode.RouteStop.NO_PICKUP_POINT,
                    messageCommon.getMessage(AppErrorCode.RouteStop.NO_PICKUP_POINT,
                            direction == RouteDirection.OUTBOUND ? "pickup" : "dropoff"));
        }

        // 5. Duplicate guard: student already assigned anywhere in this session
        if (routePlanStudentService.existsBySessionAndStudent(session.getId(), student.getId())) {
            throw new AppException(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED,
                    messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED, student.getFullName()));
        }

        // 6. Find or create a stop for the relevant point
        List<RouteStopEntity> existingStops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);

        final Long relevantPointId = relevantPoint.getId();
        RouteStopEntity stop = existingStops.stream()
                .filter(s -> s.getPickupPoint() != null && s.getPickupPoint().getId().equals(relevantPointId))
                .findFirst()
                .orElse(null);

        if (stop == null) {
            // Auto-create a new stop at the end of the route
            RouteStopType stopType = direction == RouteDirection.OUTBOUND
                    ? RouteStopType.PICKUP : RouteStopType.DROPOFF;
            stop = new RouteStopEntity();
            stop.markCreated(tenantId, actor(actorId));
            stop.setRoute(route);
            stop.setPickupPoint(relevantPoint);
            stop.setStopType(stopType);
            stop.setStopOrder(existingStops.size());
            stop.setEstimatedStudentCount(0);
            stop.setPlannedBoardingCount(0);
            stop.setPlannedDropoffCount(0);
            stop = routeStopRepository.save(stop);
            auditLogService.log(tenantId, actorId, "RoutePlan", routeId, "AUTO_CREATE_STOP",
                    "Auto-created stop at " + relevantPoint.getName());
        }

        // 7. Determine serviceAction from direction
        RoutePlanStudentAction action = direction == RouteDirection.OUTBOUND
                ? RoutePlanStudentAction.BOARD
                : RoutePlanStudentAction.DROPOFF;

        // 8. Create RoutePlanStudentEntity
        RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
        planStudent.markCreated(tenantId, actor(actorId));
        planStudent.setRoute(route);
        planStudent.setRouteStop(stop);
        planStudent.setStudent(student);
        planStudent.setSubscription(subscription);
        planStudent.setServiceAction(action);
        RoutePlanStudentEntity saved = routePlanStudentService.save(planStudent);

        // 9. Update stop boarding/dropoff counts
        if (action == RoutePlanStudentAction.BOARD) {
            stop.setPlannedBoardingCount(stop.getPlannedBoardingCount() + 1);
        } else {
            stop.setPlannedDropoffCount(stop.getPlannedDropoffCount() + 1);
        }
        routeStopRepository.save(stop);

        // 10. Update route student count
        updateRouteStudentCount(route, routeId, actorId);

        // 11. Refresh session summary counters
        planningSessionService.refreshSessionSummary(session.getId(), tenantId);

        auditLogService.log(tenantId, actorId, "RoutePlan", routeId, "ASSIGN_STUDENT",
                "Assigned student " + student.getFullName() + " at " + relevantPoint.getName());

        return mapper.toRoutePlanStudentResponse(saved);
    }

    // ── Add student to stop ───────────────────────────────────────────────────

    @Override
    @Transactional
    public RoutePlanStudentResponse addStudentToStop(Long routeId, Long stopId,
                                                     AddStudentToStopRequest request,
                                                     Long tenantId, Long actorId) {
        // 1. Load and validate route
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);

        // 2. Route must belong to a planning session
        RoutePlanningSessionEntity session = route.getPlanningSession();
        if (session == null) {
            throw new AppException(AppErrorCode.RouteStop.SESSION_NOT_LINKED,
                    messageCommon.getMessage(AppErrorCode.RouteStop.SESSION_NOT_LINKED));
        }
        if (session.getStatus() == PlanningSessionStatus.PUBLISHED
                || session.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.RouteStop.SESSION_FROZEN,
                    messageCommon.getMessage(AppErrorCode.RouteStop.SESSION_FROZEN, session.getStatus()));
        }

        // 3. Load and validate stop
        RouteStopEntity stop = routeStopRepository
                .findByIdAndTenantIdAndIsDeletedFalse(stopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, messageCommon.getMessage(AppErrorCode.NOT_FOUND)));
        if (!stop.getRoute().getId().equals(routeId)) {
            throw new AppException(AppErrorCode.RouteStop.NOT_FOUND, messageCommon.getMessage(AppErrorCode.RouteStop.NOT_FOUND));
        }

        // 4. Load and validate subscription
        StudentSubscriptionEntity subscription = subscriptionService.getSubscriptionEntity(
                request.getSubscriptionId(), tenantId);
        StudentEntity student = subscription.getStudent();
        if (!student.getId().equals(request.getStudentId())) {
            throw new AppException(AppErrorCode.RouteStop.STUDENT_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_MISMATCH));
        }

        // 5. Validate stop point matches student's relevant point for direction
        RouteDirection direction = route.getRouteDirection();
        PickupPointEntity relevantPoint = direction == RouteDirection.OUTBOUND
                ? subscription.getPickupPoint()
                : subscription.getDropoffPoint();
        if (relevantPoint == null) {
            throw new AppException(AppErrorCode.RouteStop.NO_PICKUP_POINT,
                    messageCommon.getMessage(AppErrorCode.RouteStop.NO_PICKUP_POINT,
                            direction == RouteDirection.OUTBOUND ? "pickup" : "dropoff"));
        }
        if (!stop.getPickupPoint().getId().equals(relevantPoint.getId())) {
            throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.RouteStop.INVALID_REQUEST));
        }

        // 6. Duplicate guard: student already assigned anywhere in this session
        if (routePlanStudentService.existsBySessionAndStudent(session.getId(), student.getId())) {
            throw new AppException(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED,
                    messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED, student.getFullName()));
        }

        // 7. Determine serviceAction from direction
        RoutePlanStudentAction action = direction == RouteDirection.OUTBOUND
                ? RoutePlanStudentAction.BOARD
                : RoutePlanStudentAction.DROPOFF;

        // 8. Create RoutePlanStudentEntity
        RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
        planStudent.markCreated(tenantId, actor(actorId));
        planStudent.setRoute(route);
        planStudent.setRouteStop(stop);
        planStudent.setStudent(student);
        planStudent.setSubscription(subscription);
        planStudent.setServiceAction(action);
        RoutePlanStudentEntity saved = routePlanStudentService.save(planStudent);

        // 9. Update stop boarding/dropoff counts
        if (action == RoutePlanStudentAction.BOARD) {
            stop.setPlannedBoardingCount(stop.getPlannedBoardingCount() + 1);
        } else {
            stop.setPlannedDropoffCount(stop.getPlannedDropoffCount() + 1);
        }
        routeStopRepository.save(stop);

        // 10. Update route student count
        updateRouteStudentCount(route, routeId, actorId);

        // 11. Refresh session summary counters
        planningSessionService.refreshSessionSummary(session.getId(), tenantId);

        auditLogService.log(tenantId, actorId, "RoutePlan", routeId, "ADD_STUDENT_TO_STOP",
                "Added student " + student.getFullName() + " to stop " + stop.getPickupPoint().getName());

        return mapper.toRoutePlanStudentResponse(saved);
    }

    // ── Remove stop ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeStop(Long routeId, Long stopId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);
        requireSessionEditable(route);

        RouteStopEntity stop = routeStopRepository.findByIdAndTenantIdAndIsDeletedFalse(stopId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, messageCommon.getMessage(AppErrorCode.NOT_FOUND)));

        if (!stop.getRoute().getId().equals(routeId)) {
            throw new AppException(AppErrorCode.RouteStop.NOT_FOUND, messageCommon.getMessage(AppErrorCode.RouteStop.NOT_FOUND));
        }

        // Soft-delete the stop
        stop.setIsDeleted(true);
        stop.setIsActive(false);
        stop.markUpdated(actor(actorId));
        routeStopRepository.save(stop);

        // Soft-delete all plan students linked to this stop
        List<RoutePlanStudentEntity> linkedStudents = routePlanStudentService
                .findByRouteStop(stopId);
        for (RoutePlanStudentEntity ps : linkedStudents) {
            ps.setIsDeleted(true);
            ps.setIsActive(false);
            ps.markUpdated(actor(actorId));
        }
        if (!linkedStudents.isEmpty()) {
            routePlanStudentService.saveAll(linkedStudents);
        }

        // Re-sequence remaining stops
        List<RouteStopEntity> remaining = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setStopOrder(i);
        }
        routeStopRepository.saveAll(remaining);

        // Update route metadata and geometry
        long studentCount = routePlanStudentService.countByRoute(routeId);
        route.setPlannedStudentCount((int) studentCount);
        recalculateGeometry(route, tenantId);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);

        // Refresh session summary if route belongs to a session
        if (route.getPlanningSession() != null) {
            planningSessionService.refreshSessionSummary(route.getPlanningSession().getId(), tenantId);
        }

        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "REMOVE_STOP",
                "Removed stop #" + stopId);
    }

    // ── Move student between routes ───────────────────────────────────────────

    @Override
    @Transactional
    public void moveStudent(Long sourceRouteId, MoveStudentRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity sourceRoute = routeService.getRouteEntity(sourceRouteId, tenantId);
        requireEditable(sourceRoute);
        requireSessionEditable(sourceRoute);

        RoutePlanEntity targetRoute = routeService.getRouteEntity(request.getTargetRouteId(), tenantId);
        requireEditable(targetRoute);
        requireSessionEditable(targetRoute);

        // Find the student entries in source route
        List<RoutePlanStudentEntity> sourceEntries = routePlanStudentService
                .findByRoute(sourceRouteId)
                .stream()
                .filter(ps -> ps.getStudent().getId().equals(request.getStudentId())
                        && ps.getSubscription().getId().equals(request.getSubscriptionId()))
                .toList();

        if (sourceEntries.isEmpty()) {
            throw new AppException(AppErrorCode.NOT_FOUND, messageCommon.getMessage(AppErrorCode.NOT_FOUND));
        }

        // Soft-delete from source
        for (RoutePlanStudentEntity entry : sourceEntries) {
            entry.setIsDeleted(true);
            entry.setIsActive(false);
            entry.markUpdated(actor(actorId));
        }
        routePlanStudentService.saveAll(sourceEntries);

        // Re-create in target (pick first matching stop if available)
        List<RouteStopEntity> targetStops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(request.getTargetRouteId(), tenantId);

        for (RoutePlanStudentEntity original : sourceEntries) {
            // Try to find a matching stop in the target route by pickup point
            RouteStopEntity matchStop = null;
            if (original.getRouteStop() != null) {
                Long pointId = original.getRouteStop().getPickupPoint().getId();
                matchStop = targetStops.stream()
                        .filter(s -> s.getPickupPoint().getId().equals(pointId))
                        .findFirst()
                        .orElse(null);
            }

            RoutePlanStudentEntity newEntry = new RoutePlanStudentEntity();
            newEntry.markCreated(tenantId, actor(actorId));
            newEntry.setRoute(targetRoute);
            newEntry.setRouteStop(matchStop);
            newEntry.setStudent(original.getStudent());
            newEntry.setSubscription(original.getSubscription());
            newEntry.setServiceAction(original.getServiceAction());
            newEntry.setPlannedTime(original.getPlannedTime());
            routePlanStudentService.save(newEntry);
        }

        // Update counts on both routes
        updateRouteStudentCount(sourceRoute, sourceRouteId, actorId);
        updateRouteStudentCount(targetRoute, request.getTargetRouteId(), actorId);

        auditLogService.log(tenantId, actorId, "RoutePlan", sourceRouteId, "MOVE_STUDENT",
                "Moved student #" + request.getStudentId() + " to route #" + request.getTargetRouteId());
    }

    // ── Remove student from route ─────────────────────────────────────────────

    @Override
    @Transactional
    public void removeStudent(Long routeId, Long studentId, Long subscriptionId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);
        requireSessionEditable(route);

        List<RoutePlanStudentEntity> entries = routePlanStudentService
                .findByRoute(routeId)
                .stream()
                .filter(ps -> ps.getStudent().getId().equals(studentId)
                        && ps.getSubscription().getId().equals(subscriptionId))
                .toList();

        if (entries.isEmpty()) {
            throw new AppException(AppErrorCode.NOT_FOUND, messageCommon.getMessage(AppErrorCode.NOT_FOUND));
        }

        for (RoutePlanStudentEntity entry : entries) {
            entry.setIsDeleted(true);
            entry.setIsActive(false);
            entry.markUpdated(actor(actorId));
        }
        routePlanStudentService.saveAll(entries);

        recalculateGeometry(route, tenantId);
        updateRouteStudentCount(route, routeId, actorId);

        // Refresh session summary if route belongs to a session
        if (route.getPlanningSession() != null) {
            planningSessionService.refreshSessionSummary(route.getPlanningSession().getId(), tenantId);
        }

        auditLogService.log(tenantId, actorId, "RoutePlan", routeId, "REMOVE_STUDENT",
                "Removed student #" + studentId + " from route");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void requireEditable(RoutePlanEntity route) {
        if (route.getStatus() == RouteStatus.COMPLETED
                || route.getStatus() == RouteStatus.IN_PROGRESS
                || route.getStatus() == RouteStatus.TRIP_CREATED
                || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE,
                    messageCommon.getMessage(AppErrorCode.Route.FIELD_INVALID, "route status", route.getStatus()));
        }
    }

    private void requireSessionEditable(RoutePlanEntity route) {
        RoutePlanningSessionEntity session = route.getPlanningSession();
        if (session != null
                && (session.getStatus() == PlanningSessionStatus.PUBLISHED
                || session.getStatus() == PlanningSessionStatus.CANCELLED)) {
            throw new AppException(AppErrorCode.RouteStop.SESSION_FROZEN,
                    messageCommon.getMessage(AppErrorCode.RouteStop.SESSION_FROZEN, session.getStatus()));
        }
    }

    private void updateRouteStudentCount(RoutePlanEntity route, Long routeId, Long actorId) {
        long count = routePlanStudentService.countByRoute(routeId);
        route.setPlannedStudentCount((int) count);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
    }

    /**
     * Recomputes geometry for a route and mutates the entity in-place.
     * Also updates per-stop distances on all active stops.
     * Caller is responsible for calling {@code route.markUpdated()} and persisting.
     */
    private void recalculateGeometry(RoutePlanEntity route, Long tenantId) {
        try {
            List<RouteStopEntity> currentStops = routeStopRepository
                    .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(route.getId(), tenantId);
            routeGeometryService.computeAndUpdate(route, currentStops);
            routeStopRepository.saveAll(currentStops);
        } catch (Exception ex) {
            log.warn("Geometry recomputation failed for route {}, geometry invalidated: {}",
                    route.getId(), ex.getMessage());
            route.setGeometryPath(null);
        }
    }

    @Override
    public List<RouteStopEntity> findByRoute(Long routeId, Long tenantId) {
        return routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);
    }

    @Override
    public Optional<RouteStopEntity> findRouteStop(Long stopId, Long tenantId) {
        return routeStopRepository.findByIdAndTenantIdAndIsDeletedFalse(stopId, tenantId);
    }

    @Override
    public RouteStopEntity saveRouteStop(RouteStopEntity entity) {
        return routeStopRepository.save(entity);
    }

    @Override
    public List<RouteStopEntity> saveAllRouteStops(List<RouteStopEntity> entities) {
        return routeStopRepository.saveAll(entities);
    }
}
