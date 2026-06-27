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
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.RouteStopPurpose;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.service.IPickupPointService;
import serp.project.school_bus_service.repository.RouteStopRepository;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IRouteGeometryService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.service.IRoutePlanningSessionService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.service.IStudentSubscriptionService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RouteStopServiceImpl extends AbstractBaseService<RouteStopEntity, Long>
        implements IRouteStopService {

    private final RouteStopRepository routeStopRepository;
    private final IRouteService routeService;
    private final IRoutePlanStudentService routePlanStudentService;
    private final IPickupPointService pickupPointService;
    private final ISchoolService schoolService;
    private final IDepotService depotService;
    private final IRoutePlanningSessionService planningSessionService;
    private final IStudentSubscriptionService subscriptionService;
    private final IRouteGeometryService routeGeometryService;
    private final IRouteDispatchService routeDispatchService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;

    public RouteStopServiceImpl(RouteStopRepository routeStopRepository,
            @Lazy IRouteService routeService,
            IRoutePlanStudentService routePlanStudentService,
            IPickupPointService pickupPointService,
            ISchoolService schoolService,
            IDepotService depotService,
            @Lazy IRoutePlanningSessionService planningSessionService,
            IStudentSubscriptionService subscriptionService,
            @Lazy IRouteGeometryService routeGeometryService,
            @Lazy IRouteDispatchService routeDispatchService,
            SchoolBusMapper mapper,
            MessageCommon messageCommon) {
        this.routeStopRepository = routeStopRepository;
        this.routeService = routeService;
        this.routePlanStudentService = routePlanStudentService;
        this.pickupPointService = pickupPointService;
        this.schoolService = schoolService;
        this.depotService = depotService;
        this.planningSessionService = planningSessionService;
        this.subscriptionService = subscriptionService;
        this.routeGeometryService = routeGeometryService;
        this.routeDispatchService = routeDispatchService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }

    @Override
    protected BaseRepository<RouteStopEntity, Long> getRepository() {
        return routeStopRepository;
    }

    // Reorder

    @Override
    @Transactional
    public List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId,
            Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);

        List<RouteStopEntity> allStops = hydrateLocations(routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId), tenantId);

        List<RouteStopEntity> terminals = allStops.stream()
                .filter(s -> s.getStopPurpose() != null && s.getStopPurpose().isTerminal()).toList();
        List<RouteStopEntity> middleStops = allStops.stream()
                .filter(s -> s.getStopPurpose() == null || !s.getStopPurpose().isTerminal()).toList();

        List<Long> ordered = request.getOrderedStopIds();
        List<Long> terminalIds = terminals.stream().map(RouteStopEntity::getId).toList();
        for (Long id : ordered) {
            if (terminalIds.contains(id)) {
                throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                        messageCommon.getMessage(AppErrorCode.RouteStop.INVALID_REQUEST));
            }
        }
        if (ordered.size() != middleStops.size()) {
            throw new AppException(AppErrorCode.RouteStop.COUNT_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.RouteStop.COUNT_MISMATCH));
        }

        List<RouteStopEntity> reorderedMiddle = new ArrayList<>(middleStops.size());
        for (Long stopId : ordered) {
            RouteStopEntity stop = middleStops.stream().filter(s -> s.getId().equals(stopId)).findFirst()
                    .orElseThrow(() -> new AppException(AppErrorCode.RouteStop.NOT_FOUND,
                            messageCommon.getMessage(AppErrorCode.RouteStop.NOT_FOUND)));
            reorderedMiddle.add(stop);
        }

        RouteStopEntity startTerminal = terminals.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL).findFirst().orElse(null);
        RouteStopEntity endTerminal = terminals.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL).findFirst().orElse(null);

        List<RouteStopEntity> full = new ArrayList<>();
        if (startTerminal != null)
            full.add(startTerminal);
        full.addAll(reorderedMiddle);
        if (endTerminal != null)
            full.add(endTerminal);

        saveNormalizedStops(full);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
        recalculateRouteDistance(route, tenantId);
        return hydrateLocations(routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId), tenantId)
                .stream().map(mapper::toRouteStopResponse).toList();
    }

    // Add stop

    @Override
    @Transactional
    public RouteStopResponse addStop(Long routeId, AddRouteStopRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);

        PickupPointEntity pickupPoint = pickupPointService.getPickupPoint(request.getPickupPointId(), tenantId);

        List<RouteStopEntity> existingStops = hydrateLocations(routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId), tenantId);

        boolean duplicate = existingStops.stream()
                .filter(s -> s.getPickupPoint() != null)
                .anyMatch(s -> s.getPickupPoint().getId().equals(request.getPickupPointId()));
        if (duplicate) {
            throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.RouteStop.INVALID_REQUEST));
        }

        RouteStopEntity stop = buildMiddleStop(route, pickupPoint, tenantId, actorId);
        stop.setEstimatedStudentCount(
                request.getEstimatedStudentCount() != null ? request.getEstimatedStudentCount() : 0);

        List<RouteStopEntity> middleStops = existingStops.stream()
                .filter(s -> s.getStopPurpose() == null || !s.getStopPurpose().isTerminal())
                .collect(Collectors.toCollection(ArrayList::new));
        middleStops.add(stop);

        RouteStopEntity startTerminal = existingStops.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL).findFirst().orElse(null);
        RouteStopEntity endTerminal = existingStops.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL).findFirst().orElse(null);

        List<RouteStopEntity> full = new ArrayList<>();
        if (startTerminal != null)
            full.add(startTerminal);
        full.addAll(middleStops);
        if (endTerminal != null)
            full.add(endTerminal);

        saveNormalizedStops(full);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
        recalculateRouteDistance(route, tenantId);
        return mapper.toRouteStopResponse(stop);
    }

    // Assign student to route (auto-create stop if needed)

    @Override
    @Transactional
    public RoutePlanStudentResponse assignStudentToRoute(Long routeId, AddStudentToStopRequest request,
            Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);

        RoutePlanningSessionEntity session = route.getPlanningSession();
        if (session == null) {
            throw new AppException(AppErrorCode.RouteStop.SESSION_NOT_LINKED,
                    messageCommon.getMessage(AppErrorCode.RouteStop.SESSION_NOT_LINKED));
        }
        requireSessionEditable(route);
        requireSelectedBusCapacity(route, routeId);

        StudentSubscriptionEntity subscription = subscriptionService.getSubscriptionEntity(
                request.getSubscriptionId(), tenantId);
        StudentEntity student = subscription.getStudent();
        if (!student.getId().equals(request.getStudentId())) {
            throw new AppException(AppErrorCode.RouteStop.STUDENT_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_MISMATCH));
        }

        RouteDirection direction = route.getRouteDirection();

        // Duplicate guard
        boolean assignedInOtherRoute = routePlanStudentService.existsInOtherRoutesOfSessionAndDirection(
                session.getId(), routeId, student.getId(), direction);
        if (assignedInOtherRoute) {
            throw new AppException(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED,
                    messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED, student.getFullName()));
        }

        // Determine pickup and dropoff points from subscription
        PickupPointEntity pickupPoint = subscription.getPickupPoint();
        PickupPointEntity dropoffPoint = subscription.getDropoffPoint();

        // Load current stops
        List<RouteStopEntity> existingStops = hydrateLocations(routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId), tenantId);

        // Find or auto-create the relevant stop (pickup for OUTBOUND, dropoff for
        // RETURN)
        PickupPointEntity relevantPoint = direction == RouteDirection.OUTBOUND ? pickupPoint : dropoffPoint;
        if (relevantPoint == null) {
            throw new AppException(AppErrorCode.RouteStop.NO_PICKUP_POINT,
                    messageCommon.getMessage(AppErrorCode.RouteStop.NO_PICKUP_POINT,
                            direction == RouteDirection.OUTBOUND ? "pickup" : "dropoff"));
        }

        RouteStopEntity stop = findOrCreateMiddleStop(route, relevantPoint, existingStops, tenantId, actorId);

        // Find terminal stops for pickup/dropoff assignment
        RouteStopEntity terminalStop = existingStops.stream()
                .filter(s -> s.getStopPurpose() == (direction == RouteDirection.OUTBOUND
                        ? RouteStopPurpose.END_TERMINAL
                        : RouteStopPurpose.START_TERMINAL))
                .findFirst().orElse(null);

        // Create RoutePlanStudent (1 row per student)
        RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
        planStudent.markCreated(tenantId, actor(actorId));
        planStudent.setRoute(route);
        planStudent.setStudent(student);
        planStudent.setSubscription(subscription);
        if (direction == RouteDirection.OUTBOUND) {
            planStudent.setPickupStop(stop);
            planStudent.setDropoffStop(terminalStop);
        } else {
            planStudent.setPickupStop(terminalStop);
            planStudent.setDropoffStop(stop);
        }
        RoutePlanStudentEntity saved = routePlanStudentService.save(planStudent);

        // Update stop counts
        if (direction == RouteDirection.OUTBOUND) {
            stop.setPlannedBoardingCount(stop.getPlannedBoardingCount() + 1);
        } else {
            stop.setPlannedDropoffCount(stop.getPlannedDropoffCount() + 1);
        }
        stop.setEstimatedStudentCount(stop.getPlannedBoardingCount() + stop.getPlannedDropoffCount());
        routeStopRepository.save(stop);

        // Update route and session summary
        updateRouteStudentCount(route, routeId, actorId);
        recalculateRouteDistance(route, tenantId);
        planningSessionService.refreshSessionSummary(session.getId(), tenantId);

        return mapper.toRoutePlanStudentResponse(saved);
    }

    // Add student to stop

    @Override
    @Transactional
    public RoutePlanStudentResponse addStudentToStop(Long routeId, Long stopId, AddStudentToStopRequest request,
            Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);
        requireSessionEditable(route);
        requireSelectedBusCapacity(route, routeId);

        RouteStopEntity stop = routeStopRepository.findByIdAndTenantIdAndIsDeletedFalse(stopId, tenantId)
                .map(entity -> hydrateLocation(entity, tenantId))
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        messageCommon.getMessage(AppErrorCode.NOT_FOUND)));
        if (!stop.getRoute().getId().equals(routeId)) {
            throw new AppException(AppErrorCode.RouteStop.NOT_FOUND,
                    messageCommon.getMessage(AppErrorCode.RouteStop.NOT_FOUND));
        }

        StudentSubscriptionEntity subscription = subscriptionService.getSubscriptionEntity(
                request.getSubscriptionId(), tenantId);
        StudentEntity student = subscription.getStudent();
        if (!student.getId().equals(request.getStudentId())) {
            throw new AppException(AppErrorCode.RouteStop.STUDENT_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_MISMATCH));
        }

        RoutePlanningSessionEntity session = route.getPlanningSession();
        RouteDirection direction = route.getRouteDirection();

        // Duplicate guard
        if (session != null) {
            boolean assignedInOtherRoute = routePlanStudentService.existsInOtherRoutesOfSessionAndDirection(
                    session.getId(), routeId, student.getId(), direction);
            if (assignedInOtherRoute) {
                throw new AppException(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED,
                        messageCommon.getMessage(AppErrorCode.RouteStop.STUDENT_ALREADY_ASSIGNED,
                                student.getFullName()));
            }
        }

        // Find terminal stop
        List<RouteStopEntity> existingStops = hydrateLocations(routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId), tenantId);
        RouteStopEntity terminalStop = existingStops.stream()
                .filter(s -> s.getStopPurpose() == (direction == RouteDirection.OUTBOUND
                        ? RouteStopPurpose.END_TERMINAL
                        : RouteStopPurpose.START_TERMINAL))
                .findFirst().orElse(null);

        // Create RoutePlanStudent
        RoutePlanStudentEntity planStudent = new RoutePlanStudentEntity();
        planStudent.markCreated(tenantId, actor(actorId));
        planStudent.setRoute(route);
        planStudent.setStudent(student);
        planStudent.setSubscription(subscription);
        if (direction == RouteDirection.OUTBOUND) {
            planStudent.setPickupStop(stop);
            planStudent.setDropoffStop(terminalStop);
        } else {
            planStudent.setPickupStop(terminalStop);
            planStudent.setDropoffStop(stop);
        }
        RoutePlanStudentEntity saved = routePlanStudentService.save(planStudent);

        // Update stop counts
        if (direction == RouteDirection.OUTBOUND) {
            stop.setPlannedBoardingCount(stop.getPlannedBoardingCount() + 1);
        } else {
            stop.setPlannedDropoffCount(stop.getPlannedDropoffCount() + 1);
        }
        stop.setEstimatedStudentCount(stop.getPlannedBoardingCount() + stop.getPlannedDropoffCount());
        routeStopRepository.save(stop);

        updateRouteStudentCount(route, routeId, actorId);
        recalculateRouteDistance(route, tenantId);
        if (session != null) {
            planningSessionService.refreshSessionSummary(session.getId(), tenantId);
        }

        return mapper.toRoutePlanStudentResponse(saved);
    }

    // Remove stop

    @Override
    @Transactional
    public void removeStop(Long routeId, Long stopId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);

        RouteStopEntity stop = routeStopRepository.findByIdAndTenantIdAndIsDeletedFalse(stopId, tenantId)
                .map(entity -> hydrateLocation(entity, tenantId))
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND,
                        messageCommon.getMessage(AppErrorCode.NOT_FOUND)));
        if (!stop.getRoute().getId().equals(routeId)) {
            throw new AppException(AppErrorCode.RouteStop.NOT_FOUND,
                    messageCommon.getMessage(AppErrorCode.RouteStop.NOT_FOUND));
        }
        if (stop.getStopPurpose() != null && stop.getStopPurpose().isTerminal()) {
            throw new AppException(AppErrorCode.RouteStop.INVALID_REQUEST,
                    messageCommon.getMessage(AppErrorCode.RouteStop.INVALID_REQUEST));
        }

        // Remove students assigned to this stop
        List<RoutePlanStudentEntity> affected = routePlanStudentService.findByRoute(routeId).stream()
                .filter(ps -> (ps.getPickupStop() != null && ps.getPickupStop().getId().equals(stopId))
                        || (ps.getDropoffStop() != null && ps.getDropoffStop().getId().equals(stopId)))
                .toList();
        for (RoutePlanStudentEntity ps : affected) {
            ps.markSoftDeleted(actor(actorId));
            routePlanStudentService.save(ps);
        }

        // Soft-delete stop
        stop.markSoftDeleted(actor(actorId));
        routeStopRepository.save(stop);

        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
        recalculateRouteDistance(route, tenantId);

        if (route.getPlanningSession() != null) {
            planningSessionService.refreshSessionSummary(route.getPlanningSession().getId(), tenantId);
        }
    }

    // Move student

    @Override
    @Transactional
    public void moveStudent(Long sourceRouteId, MoveStudentRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity sourceRoute = routeService.getRouteEntity(sourceRouteId, tenantId);
        RoutePlanEntity targetRoute = routeService.getRouteEntity(request.getTargetRouteId(), tenantId);
        requireEditable(sourceRoute);
        requireEditable(targetRoute);

        // Find the student entry in source route
        List<RoutePlanStudentEntity> sourceEntries = routePlanStudentService.findByRoute(sourceRouteId).stream()
                .filter(ps -> ps.getStudent().getId().equals(request.getStudentId())
                        && ps.getSubscription().getId().equals(request.getSubscriptionId())
                        && !Boolean.TRUE.equals(ps.getIsDeleted()))
                .toList();

        if (sourceEntries.isEmpty()) {
            throw new AppException(AppErrorCode.NOT_FOUND,
                    messageCommon.getMessage(AppErrorCode.NOT_FOUND));
        }

        // Soft-delete from source
        for (RoutePlanStudentEntity entry : sourceEntries) {
            entry.markSoftDeleted(actor(actorId));
            routePlanStudentService.save(entry);
        }

        // Re-create in target route using assign logic
        AddStudentToStopRequest addReq = new AddStudentToStopRequest();
        addReq.setStudentId(request.getStudentId());
        addReq.setSubscriptionId(request.getSubscriptionId());
        assignStudentToRoute(request.getTargetRouteId(), addReq, tenantId, actorId);

        // Update source route
        updateRouteStudentCount(sourceRoute, sourceRouteId, actorId);
        if (sourceRoute.getPlanningSession() != null) {
            planningSessionService.refreshSessionSummary(sourceRoute.getPlanningSession().getId(), tenantId);
        }
    }

    // Remove student

    @Override
    @Transactional
    public void removeStudent(Long routeId, Long studentId, Long subscriptionId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routeService.getRouteEntity(routeId, tenantId);
        requireEditable(route);

        List<RoutePlanStudentEntity> entries = routePlanStudentService.findByRoute(routeId).stream()
                .filter(ps -> ps.getStudent().getId().equals(studentId)
                        && ps.getSubscription().getId().equals(subscriptionId)
                        && !Boolean.TRUE.equals(ps.getIsDeleted()))
                .toList();

        if (entries.isEmpty()) {
            throw new AppException(AppErrorCode.NOT_FOUND,
                    messageCommon.getMessage(AppErrorCode.NOT_FOUND));
        }

        for (RoutePlanStudentEntity entry : entries) {
            // Update stop counts
            if (entry.getPickupStop() != null && entry.getPickupStop().getStopPurpose() != null
                    && !entry.getPickupStop().getStopPurpose().isTerminal()) {
                RouteStopEntity stop = entry.getPickupStop();
                stop.setPlannedBoardingCount(Math.max(0, stop.getPlannedBoardingCount() - 1));
                stop.setEstimatedStudentCount(stop.getPlannedBoardingCount() + stop.getPlannedDropoffCount());
                softDeleteIfEmptyMiddleStop(stop, actorId);
                routeStopRepository.save(stop);
            }
            if (entry.getDropoffStop() != null && entry.getDropoffStop().getStopPurpose() != null
                    && !entry.getDropoffStop().getStopPurpose().isTerminal()) {
                RouteStopEntity stop = entry.getDropoffStop();
                stop.setPlannedDropoffCount(Math.max(0, stop.getPlannedDropoffCount() - 1));
                stop.setEstimatedStudentCount(stop.getPlannedBoardingCount() + stop.getPlannedDropoffCount());
                softDeleteIfEmptyMiddleStop(stop, actorId);
                routeStopRepository.save(stop);
            }
            entry.markSoftDeleted(actor(actorId));
            routePlanStudentService.save(entry);
        }

        updateRouteStudentCount(route, routeId, actorId);

        // Normalize remaining active stops to avoid gaps in stop order
        List<RouteStopEntity> remainingStops = hydrateLocations(routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId), tenantId);
        saveNormalizedStops(remainingStops);

        recalculateRouteDistance(route, tenantId);
        if (route.getPlanningSession() != null) {
            planningSessionService.refreshSessionSummary(route.getPlanningSession().getId(), tenantId);
        }
    }

    private RouteStopEntity buildMiddleStop(RoutePlanEntity route, PickupPointEntity pickupPoint,
            Long tenantId, Long actorId) {
        RouteStopEntity stop = new RouteStopEntity();
        stop.markCreated(tenantId, actor(actorId));
        stop.setRoute(route);
        stop.setPickupPoint(pickupPoint);
        stop.setLocationType(RouteLocationType.PICKUP_POINT);
        stop.setStopPurpose(route.getRouteDirection() == RouteDirection.OUTBOUND
                ? RouteStopPurpose.PICKUP
                : RouteStopPurpose.DROPOFF);
        stop.setEstimatedStudentCount(0);
        stop.setPlannedBoardingCount(0);
        stop.setPlannedDropoffCount(0);
        return stop;
    }

    private RouteStopEntity findOrCreateMiddleStop(RoutePlanEntity route, PickupPointEntity point,
            List<RouteStopEntity> existingStops,
            Long tenantId, Long actorId) {
        return existingStops.stream()
                .filter(s -> s.getPickupPoint() != null && s.getPickupPoint().getId().equals(point.getId()))
                .findFirst()
                .orElseGet(() -> {
                    RouteStopEntity newStop = buildMiddleStop(route, point, tenantId, actorId);
                    List<RouteStopEntity> middleStops = existingStops.stream()
                            .filter(s -> s.getStopPurpose() == null || !s.getStopPurpose().isTerminal())
                            .collect(Collectors.toCollection(ArrayList::new));
                    middleStops.add(newStop);

                    RouteStopEntity startTerminal = existingStops.stream()
                            .filter(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL).findFirst()
                            .orElse(null);
                    RouteStopEntity endTerminal = existingStops.stream()
                            .filter(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL).findFirst().orElse(null);

                    List<RouteStopEntity> full = new ArrayList<>();
                    if (startTerminal != null)
                        full.add(startTerminal);
                    full.addAll(middleStops);
                    if (endTerminal != null)
                        full.add(endTerminal);
                    saveNormalizedStops(full);
                    return newStop;
                });
    }

    private List<RouteStopEntity> saveNormalizedStops(List<RouteStopEntity> fullOrderedList) {
        List<RouteStopEntity> persisted = fullOrderedList.stream()
                .filter(s -> s.getId() != null).collect(Collectors.toList());
        for (int i = 0; i < persisted.size(); i++) {
            persisted.get(i).setStopOrder(-(1000 + i));
        }
        routeStopRepository.saveAllAndFlush(persisted);
        for (int i = 0; i < fullOrderedList.size(); i++) {
            fullOrderedList.get(i).setStopOrder(i);
        }
        return routeStopRepository.saveAll(fullOrderedList);
    }

    private void updateRouteStudentCount(RoutePlanEntity route, Long routeId, Long actorId) {
        long count = routePlanStudentService.countDistinctStudentsByRoute(routeId);
        route.setPlannedStudentCount((int) count);
        route.setRequiredCapacity((int) count);
        route.markUpdated(actor(actorId));
        routeService.saveRouteEntity(route);
    }

    private void softDeleteIfEmptyMiddleStop(RouteStopEntity stop, Long actorId) {
        int plannedCount = (stop.getPlannedBoardingCount() == null ? 0 : stop.getPlannedBoardingCount())
                + (stop.getPlannedDropoffCount() == null ? 0 : stop.getPlannedDropoffCount());
        if (plannedCount == 0 && stop.getStopPurpose() != null && !stop.getStopPurpose().isTerminal()) {
            stop.markSoftDeleted(actor(actorId));
        }
    }

    private void requireSelectedBusCapacity(RoutePlanEntity route, Long routeId) {
        Integer capacity = routeDispatchService.findAssignmentEntityByRoute(routeId, route.getTenantId())
                .map(assignment -> assignment.getBus().getCapacity())
                .orElse(null);
        if (capacity == null && route.getSelectedBus() != null) {
            capacity = route.getSelectedBus().getCapacity();
        }
        if (capacity == null) {
            throw new AppException(AppErrorCode.Bus.SELECTED_BUS_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Bus.SELECTED_BUS_REQUIRED));
        }
        long currentStudentCount = routePlanStudentService.countDistinctStudentsByRoute(routeId);
        if (currentStudentCount + 1 > capacity) {
            throw new AppException(AppErrorCode.Bus.CAPACITY_EXCEEDED,
                    messageCommon.getMessage(AppErrorCode.Bus.CAPACITY_EXCEEDED, capacity, currentStudentCount));
        }
    }

    private void requireEditable(RoutePlanEntity route) {
        if (route.getStatus() == RouteStatus.TRIP_CREATED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE,
                    messageCommon.getMessage(AppErrorCode.Route.INVALID_STATE));
        }
    }

    private void requireSessionEditable(RoutePlanEntity route) {
        RoutePlanningSessionEntity session = route.getPlanningSession();
        if (session != null && (session.getStatus() == PlanningSessionStatus.PUBLISHED
                || session.getStatus() == PlanningSessionStatus.CANCELLED)) {
            throw new AppException(AppErrorCode.RouteStop.SESSION_FROZEN,
                    messageCommon.getMessage(AppErrorCode.RouteStop.SESSION_FROZEN, session.getStatus()));
        }
    }

    @Override
    public List<RouteStopEntity> findByRoute(Long routeId, Long tenantId) {
        return hydrateLocations(
                routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId),
                tenantId);
    }

    @Override
    public Optional<RouteStopEntity> findRouteStop(Long stopId, Long tenantId) {
        return routeStopRepository.findByIdAndTenantIdAndIsDeletedFalse(stopId, tenantId)
                .map(stop -> hydrateLocation(stop, tenantId));
    }

    @Override
    public RouteStopEntity saveRouteStop(RouteStopEntity entity) {
        return routeStopRepository.save(entity);
    }

    @Override
    public List<RouteStopEntity> saveAllRouteStops(List<RouteStopEntity> entities) {
        return routeStopRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void deletePhysical(Long id) {
        routeStopRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateTerminalStops(RoutePlanEntity route, Long tenantId, Long actorId) {
        List<RouteStopEntity> stops = hydrateLocations(routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(route.getId(), tenantId), tenantId);

        RouteStopEntity startTerminal = stops.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.START_TERMINAL)
                .findFirst()
                .orElseGet(() -> {
                    RouteStopEntity newStop = new RouteStopEntity();
                    newStop.markCreated(tenantId, actor(actorId));
                    newStop.setRoute(route);
                    newStop.setStopPurpose(RouteStopPurpose.START_TERMINAL);
                    newStop.setEstimatedStudentCount(0);
                    newStop.setPlannedBoardingCount(0);
                    newStop.setPlannedDropoffCount(0);
                    return newStop;
                });

        RouteStopEntity endTerminal = stops.stream()
                .filter(s -> s.getStopPurpose() == RouteStopPurpose.END_TERMINAL)
                .findFirst()
                .orElseGet(() -> {
                    RouteStopEntity newStop = new RouteStopEntity();
                    newStop.markCreated(tenantId, actor(actorId));
                    newStop.setRoute(route);
                    newStop.setStopPurpose(RouteStopPurpose.END_TERMINAL);
                    newStop.setEstimatedStudentCount(0);
                    newStop.setPlannedBoardingCount(0);
                    newStop.setPlannedDropoffCount(0);
                    return newStop;
                });

        // Update Start Terminal
        startTerminal.setLocationType(route.getStartLocationType());
        if (route.getStartLocationType() == RouteLocationType.SCHOOL) {
            startTerminal.setSchool(route.getStartSchool());
            startTerminal.setDepot(null);
            startTerminal.setPickupPoint(null);
        } else {
            startTerminal.setDepot(route.getStartDepot());
            startTerminal.setSchool(null);
            startTerminal.setPickupPoint(null);
        }
        if (startTerminal.getId() != null) {
            startTerminal.markUpdated(actor(actorId));
        }

        // Update End Terminal
        endTerminal.setLocationType(route.getEndLocationType());
        if (route.getEndLocationType() == RouteLocationType.SCHOOL) {
            endTerminal.setSchool(route.getEndSchool());
            endTerminal.setDepot(null);
            endTerminal.setPickupPoint(null);
        } else {
            endTerminal.setDepot(route.getEndDepot());
            endTerminal.setSchool(null);
            endTerminal.setPickupPoint(null);
        }
        if (endTerminal.getId() != null) {
            endTerminal.markUpdated(actor(actorId));
        }

        // Get all middle stops
        List<RouteStopEntity> middleStops = stops.stream()
                .filter(s -> s.getStopPurpose() != null && !s.getStopPurpose().isTerminal())
                .collect(Collectors.toList());

        List<RouteStopEntity> fullOrderedList = new java.util.ArrayList<>();
        fullOrderedList.add(startTerminal);
        fullOrderedList.addAll(middleStops);
        fullOrderedList.add(endTerminal);

        saveNormalizedStops(fullOrderedList);
        recalculateRouteDistance(route, tenantId);
    }

    // Geometry: delegate to IRouteGeometryService (OSRM)

    private void recalculateRouteDistance(RoutePlanEntity route, Long tenantId) {
        routeGeometryService.recalculateGeometry(route, tenantId);
    }

    private List<RouteStopEntity> hydrateLocations(List<RouteStopEntity> stops, Long tenantId) {
        return stops.stream()
                .map(stop -> hydrateLocation(stop, tenantId))
                .toList();
    }

    @Override
    public RouteStopEntity hydrateLocation(RouteStopEntity stop, Long tenantId) {
        if (stop == null || stop.getLocationType() == null || stop.getLocationId() == null) {
            return stop;
        }
        switch (stop.getLocationType()) {
            case PICKUP_POINT -> stop.setPickupPoint(pickupPointService.getPickupPoint(stop.getLocationId(), tenantId));
            case SCHOOL -> stop.setSchool(schoolService.getSchool(stop.getLocationId(), tenantId));
            case DEPOT -> stop.setDepot(depotService.getDepot(stop.getLocationId(), tenantId));
        }
        return stop;
    }
}
