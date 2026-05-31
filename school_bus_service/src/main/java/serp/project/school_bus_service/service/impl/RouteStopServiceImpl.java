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
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.repository.RouteStopRepository;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.domain.RouteStopFactory;
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
import java.util.Set;
import java.util.stream.Collectors;

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
    private final RouteStopFactory routeStopFactory;
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
                                RouteStopFactory routeStopFactory,
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
        this.routeStopFactory = routeStopFactory;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }


    @Override
    protected BaseRepository<RouteStopEntity, Long> getRepository() {
        return routeStopRepository;
    }

    // ── Reorder ───────────────────────────────────────────────────────────────

    /**
     * Reorders middle stops only. The request must contain exactly the IDs of the
     * non-terminal stops in the desired order. Terminals are never included in the
     * payload — the backend automatically pins START_TERMINAL at 0 and END_TERMINAL
     * at N+1 after normalising.
     */
    @Override
    @Transactional
    public List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId,
            Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);
        requireSessionEditable(route);

        List<RouteStopEntity> allStops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);

        List<RouteStopEntity> terminals = allStops.stream()
                .filter(s -> s.getStopPurpose() != null && s.getStopPurpose().isTerminal())
                .toList();
        List<RouteStopEntity> middleStops = allStops.stream()
                .filter(s -> s.getStopPurpose() == null || !s.getStopPurpose().isTerminal())
                .toList();

        List<Long> ordered = request.getOrderedStopIds();
        // Reject if payload contains terminal IDs
        List<Long> terminalIds = terminals.stream().map(RouteStopEntity::getId).toList();
        for (Long id : ordered) {
            if (terminalIds.contains(id)) {
                throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                        "Terminal stops cannot be reordered — only supply middle stop IDs");
            }
        }
        if (ordered.size() != middleStops.size()) {
            throw new AppException(AppErrorCode.RouteStop.COUNT_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.RouteStop.COUNT_MISMATCH));
        }

        // Apply new middle order and rebuild full ordered list
        List<RouteStopEntity> reorderedMiddle = new ArrayList<>(middleStops.size());
        for (Long stopId : ordered) {
            RouteStopEntity stop = middleStops.stream().filter(s -> s.getId().equals(stopId)).findFirst()
                    .orElseThrow(() -> new AppException(AppErrorCode.RouteStop.NOT_FOUND,
                            messageCommon.getMessage(AppErrorCode.RouteStop.NOT_FOUND)));
            reorderedMiddle.add(stop);
        }

        // Rebuild full list: START_TERMINAL + reordered middle + END_TERMINAL
        RouteStopEntity startTerminal = terminals.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL).findFirst().orElse(null);
        RouteStopEntity endTerminal = terminals.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL).findFirst().orElse(null);

        List<RouteStopEntity> full = new ArrayList<>();
        if (startTerminal != null) full.add(startTerminal);
        full.addAll(reorderedMiddle);
        if (endTerminal != null) full.add(endTerminal);

        saveNormalizedStops(full);
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

        // Only middle stops (PICKUP/DROPOFF) may be added manually — terminals are auto-managed
        boolean duplicate = existingStops.stream()
                .filter(s -> s.getPickupPoint() != null)
                .anyMatch(s -> s.getPickupPoint().getId().equals(request.getPickupPointId()));
        if (duplicate) {
            throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                    "Pickup point already exists on this route");
        }

        // Build new middle stop using factory
        RouteStopEntity stop = routeStopFactory.buildMiddleStop(route, pickupPoint, tenantId, actor(actorId));
        stop.setEstimatedStudentCount(request.getEstimatedStudentCount() != null ? request.getEstimatedStudentCount() : 0);

        // Insert before END_TERMINAL: gather existing middle stops + new stop, rebuild full list
        List<RouteStopEntity> middleStops = existingStops.stream()
                .filter(s -> s.getStopPurpose() == null || !s.getStopPurpose().isTerminal())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        middleStops.add(stop);

        // Re-attach terminals
        RouteStopEntity startTerminal = existingStops.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL).findFirst().orElse(null);
        RouteStopEntity endTerminal = existingStops.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL).findFirst().orElse(null);

        List<RouteStopEntity> full = new ArrayList<>();
        if (startTerminal != null) full.add(startTerminal);
        full.addAll(middleStops);
        if (endTerminal != null) full.add(endTerminal);

        // Two-phase save: avoids unique constraint violation on (route_id, stop_order)
        saveNormalizedStops(full);
        // stop is in the full list — its id is set by JPA after saveAll above

        recalculateGeometry(route, tenantId);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);

        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "ADD_STOP",
                "Added stop: " + pickupPoint.getName());
        return mapper.toRouteStopResponse(stop);
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

        // 6. Load current stops
        List<RouteStopEntity> existingStops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);

        // 7. Find or auto-create middle stop for the relevant pickup/dropoff point
        final Long relevantPointId = relevantPoint.getId();
        RouteStopEntity stop = existingStops.stream()
                .filter(s -> s.getPickupPoint() != null && s.getPickupPoint().getId().equals(relevantPointId))
                .findFirst()
                .orElse(null);

        if (stop == null) {
            // Auto-create a new middle stop using factory
            stop = routeStopFactory.buildMiddleStop(route, relevantPoint, tenantId, actor(actorId));

            // Insert before END_TERMINAL, normalize, save
            List<RouteStopEntity> middleStops = existingStops.stream()
                    .filter(s -> s.getStopPurpose() == null || !s.getStopPurpose().isTerminal())
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            middleStops.add(stop);

            RouteStopEntity startTerminal = existingStops.stream()
                    .filter(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL).findFirst().orElse(null);
            RouteStopEntity endTerminal = existingStops.stream()
                    .filter(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL).findFirst().orElse(null);

            List<RouteStopEntity> full = new ArrayList<>();
            if (startTerminal != null) full.add(startTerminal);
            full.addAll(middleStops);
            if (endTerminal != null) full.add(endTerminal);

            // Two-phase save: avoids unique constraint violation on (route_id, stop_order)
            saveNormalizedStops(full);
            // stop is in the full list — its id is set by JPA after saveAll above

            auditLogService.log(tenantId, actorId, "RoutePlan", routeId, "AUTO_CREATE_STOP",
                    "Auto-created stop at " + relevantPoint.getName());
        }

        // 8. Determine action
        RoutePlanStudentAction action = direction == RouteDirection.OUTBOUND
                ? RoutePlanStudentAction.BOARD
                : RoutePlanStudentAction.DROPOFF;

        // 9. Create RoutePlanStudentEntity — main action
        RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
        planStudent.markCreated(tenantId, actor(actorId));
        planStudent.setRoute(route);
        planStudent.setRouteStop(stop);
        planStudent.setStudent(student);
        planStudent.setSubscription(subscription);
        planStudent.setServiceAction(action);
        RoutePlanStudentEntity saved = routePlanStudentService.save(planStudent);

        // 9b. Create complementary terminal RoutePlanStudentEntity
        //     OUTBOUND: DROPOFF at END_TERMINAL (school) | RETURN: BOARD at START_TERMINAL (school)
        RouteStopPurpose terminalPurpose = direction == RouteDirection.OUTBOUND
                ? RouteStopPurpose.END_TERMINAL
                : RouteStopPurpose.START_TERMINAL;
        RoutePlanStudentAction terminalAction = direction == RouteDirection.OUTBOUND
                ? RoutePlanStudentAction.DROPOFF
                : RoutePlanStudentAction.BOARD;
        existingStops.stream()
                .filter(s -> s.getStopPurpose() == terminalPurpose)
                .findFirst()
                .ifPresent(terminalStop -> {
                    RoutePlanStudentEntity terminalEntry = new RoutePlanStudentEntity();
                    terminalEntry.markCreated(tenantId, actor(actorId));
                    terminalEntry.setRoute(route);
                    terminalEntry.setRouteStop(terminalStop);
                    terminalEntry.setStudent(student);
                    terminalEntry.setSubscription(subscription);
                    terminalEntry.setServiceAction(terminalAction);
                    routePlanStudentService.save(terminalEntry);
                });

        // 10. Update stop counts (middle stop only)
        if (action == RoutePlanStudentAction.BOARD) {
            stop.setPlannedBoardingCount(stop.getPlannedBoardingCount() + 1);
        } else {
            stop.setPlannedDropoffCount(stop.getPlannedDropoffCount() + 1);
        }
        stop.setEstimatedStudentCount(stop.getPlannedBoardingCount() + stop.getPlannedDropoffCount());
        routeStopRepository.save(stop);

        // 11. Recompute route geometry
        recalculateGeometry(route, tenantId);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);

        // 12. Update route and session summary
        updateRouteStudentCount(route, routeId, actorId);
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
        if (stop.getPickupPoint() == null || !stop.getPickupPoint().getId().equals(relevantPoint.getId())) {
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

        // 8. Create RoutePlanStudentEntity — main action
        RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
        planStudent.markCreated(tenantId, actor(actorId));
        planStudent.setRoute(route);
        planStudent.setRouteStop(stop);
        planStudent.setStudent(student);
        planStudent.setSubscription(subscription);
        planStudent.setServiceAction(action);
        RoutePlanStudentEntity saved = routePlanStudentService.save(planStudent);

        // 8b. Create complementary terminal RoutePlanStudentEntity
        //     OUTBOUND: DROPOFF at END_TERMINAL (school) | RETURN: BOARD at START_TERMINAL (school)
        RouteStopPurpose terminalPurpose2 = direction == RouteDirection.OUTBOUND
                ? RouteStopPurpose.END_TERMINAL
                : RouteStopPurpose.START_TERMINAL;
        RoutePlanStudentAction terminalAction2 = direction == RouteDirection.OUTBOUND
                ? RoutePlanStudentAction.DROPOFF
                : RoutePlanStudentAction.BOARD;
        routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId)
                .stream()
                .filter(s -> s.getStopPurpose() == terminalPurpose2)
                .findFirst()
                .ifPresent(terminalStop -> {
                    RoutePlanStudentEntity terminalEntry = new RoutePlanStudentEntity();
                    terminalEntry.markCreated(tenantId, actor(actorId));
                    terminalEntry.setRoute(route);
                    terminalEntry.setRouteStop(terminalStop);
                    terminalEntry.setStudent(student);
                    terminalEntry.setSubscription(subscription);
                    terminalEntry.setServiceAction(terminalAction2);
                    routePlanStudentService.save(terminalEntry);
                });

        // 9. Update stop boarding/dropoff counts and keep estimatedStudentCount in sync
        if (action == RoutePlanStudentAction.BOARD) {
            stop.setPlannedBoardingCount(stop.getPlannedBoardingCount() + 1);
        } else {
            stop.setPlannedDropoffCount(stop.getPlannedDropoffCount() + 1);
        }
        stop.setEstimatedStudentCount(stop.getPlannedBoardingCount() + stop.getPlannedDropoffCount());
        routeStopRepository.save(stop);

        // 10. Update route student count
        updateRouteStudentCount(route, routeId, actorId);

        // 11. Refresh session summary counters
        planningSessionService.refreshSessionSummary(session.getId(), tenantId);

        auditLogService.log(tenantId, actorId, "RoutePlan", routeId, "ADD_STUDENT_TO_STOP",
                "Added student " + student.getFullName() + " to stop " + stop.getDisplayName());

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

        // Terminal stops may not be removed manually
        if (stop.getStopPurpose() != null && stop.getStopPurpose().isTerminal()) {
            throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.RouteStop.INVALID_REQUEST));
        }

        // Soft-delete the stop
        stop.setIsDeleted(true);
        stop.setIsActive(false);
        stop.markUpdated(actor(actorId));
        routeStopRepository.save(stop);

        // Soft-delete all plan students linked to this stop, plus their complementary terminal entries
        List<RoutePlanStudentEntity> linkedStudents = routePlanStudentService
                .findByRouteStop(stopId);
        if (!linkedStudents.isEmpty()) {
            Set<String> affectedPairs = linkedStudents.stream()
                    .map(ps -> ps.getStudent().getId() + ":" + ps.getSubscription().getId())
                    .collect(Collectors.toSet());
            List<RoutePlanStudentEntity> terminalEntries = routePlanStudentService.findByRoute(routeId)
                    .stream()
                    .filter(ps -> ps.getRouteStop() != null
                            && ps.getRouteStop().getStopPurpose() != null
                            && ps.getRouteStop().getStopPurpose().isTerminal()
                            && affectedPairs.contains(ps.getStudent().getId() + ":" + ps.getSubscription().getId()))
                    .collect(Collectors.toList());
            List<RoutePlanStudentEntity> allToDelete = new ArrayList<>(linkedStudents);
            allToDelete.addAll(terminalEntries);
            for (RoutePlanStudentEntity ps : allToDelete) {
                ps.setIsDeleted(true);
                ps.setIsActive(false);
                ps.markUpdated(actor(actorId));
            }
            routePlanStudentService.saveAll(allToDelete);
        }

        // Re-sequence remaining stops (two-phase to avoid unique constraint violation)
        List<RouteStopEntity> remaining = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);
        saveNormalizedStops(remaining);

        // Update route metadata and geometry
        long studentCount = routePlanStudentService.countDistinctStudentsByRoute(routeId);
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
            // Re-link: terminal entries → find matching terminal purpose in target;
            //          middle stop entries → find matching pickup point in target.
            RouteStopEntity matchStop = null;
            if (original.getRouteStop() != null) {
                if (original.getRouteStop().getStopPurpose() != null
                        && original.getRouteStop().getStopPurpose().isTerminal()) {
                    RouteStopPurpose targetPurpose = original.getRouteStop().getStopPurpose();
                    matchStop = targetStops.stream()
                            .filter(s -> s.getStopPurpose() == targetPurpose)
                            .findFirst()
                            .orElse(null);
                } else if (original.getRouteStop().getPickupPoint() != null) {
                    Long pointId = original.getRouteStop().getPickupPoint().getId();
                    matchStop = targetStops.stream()
                            .filter(s -> s.getPickupPoint() != null && s.getPickupPoint().getId().equals(pointId))
                            .findFirst()
                            .orElse(null);
                }
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

            // Decrement middle-stop planned count (skip terminal stops — their counts are implicit)
            RouteStopEntity stop = entry.getRouteStop();
            if (stop != null && (stop.getStopPurpose() == null || !stop.getStopPurpose().isTerminal())) {
                if (entry.getServiceAction() == RoutePlanStudentAction.BOARD) {
                    stop.setPlannedBoardingCount(Math.max(0, stop.getPlannedBoardingCount() - 1));
                } else {
                    stop.setPlannedDropoffCount(Math.max(0, stop.getPlannedDropoffCount() - 1));
                }
                stop.setEstimatedStudentCount(stop.getPlannedBoardingCount() + stop.getPlannedDropoffCount());
                routeStopRepository.save(stop);
            }
        }
        routePlanStudentService.saveAll(entries);

        // Auto-remove middle stops that now have zero students
        for (RoutePlanStudentEntity entry : entries) {
            RouteStopEntity stop = entry.getRouteStop();
            if (stop != null && stop.getStopPurpose() != null && !stop.getStopPurpose().isTerminal()) {
                long remaining = routePlanStudentService.findByRouteStop(stop.getId())
                        .stream().filter(ps -> !ps.getIsDeleted()).count();
                if (remaining == 0) {
                    stop.setIsDeleted(true);
                    stop.setIsActive(false);
                    stop.markUpdated(actor(actorId));
                    routeStopRepository.save(stop);
                    auditLogService.log(tenantId, actorId, "RoutePlan", routeId, "AUTO_REMOVE_STOP",
                            "Auto-removed empty stop: " + stop.getDisplayName());
                }
            }
        }

        // Re-normalize stop orders after possible stop removal (two-phase to avoid unique constraint violation)
        List<RouteStopEntity> remaining = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);
        saveNormalizedStops(remaining);

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
        if (route.getStatus() == RouteStatus.TRIP_CREATED
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
        long count = routePlanStudentService.countDistinctStudentsByRoute(routeId);
        route.setPlannedStudentCount((int) count);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
    }

    /**
     * Two-phase safe save that avoids unique constraint violations on
     * {@code (route_id, stop_order)} when renumbering stops.
     *
     * <p>Phase 1: parks all already-persisted stops at temporary negative orders
     * and flushes, releasing their unique slots in the DB.
     * <p>Phase 2: assigns final 0-based orders via {@link RouteStopFactory#normalizeStopOrders}
     * and saves everything (including any new, unsaved stop in the list).
     *
     * @param fullOrderedList the complete, ordered stop list for the route
     *                        (must be in desired final order; may include unsaved new stops)
     * @return the saved entities in the same order
     */
    private List<RouteStopEntity> saveNormalizedStops(List<RouteStopEntity> fullOrderedList) {
        // Phase 1: park persisted stops at temp negative orders to free unique slots
        List<RouteStopEntity> persisted = fullOrderedList.stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toList());
        for (int i = 0; i < persisted.size(); i++) {
            persisted.get(i).setStopOrder(-(1000 + i));
        }
        routeStopRepository.saveAllAndFlush(persisted);

        // Phase 2: assign final 0-based orders and save all (incl. newly created stops)
        routeStopFactory.normalizeStopOrders(fullOrderedList);
        return routeStopRepository.saveAll(fullOrderedList);
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
