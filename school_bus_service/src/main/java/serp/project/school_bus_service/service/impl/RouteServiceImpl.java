package serp.project.school_bus_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.dto.request.*;
import serp.project.school_bus_service.dto.response.AssignmentHistoryResponse;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.IAuditLogService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.domain.RouteStopFactory;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.service.ISchoolScheduleService;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolScheduleEntity;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.ShiftType;
import serp.project.school_bus_service.repository.RoutePlanRepository;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;

import serp.project.school_bus_service.dto.response.RouteIssueDetailResponse;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

/**
 * Route service facade. Retains CRUD, query, and manifest logic directly.
 * Delegates stop operations, dispatch, and lifecycle to dedicated services.
 */
@Service
public class RouteServiceImpl extends AbstractBaseService<RoutePlanEntity, Long> implements IRouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);

    private final RoutePlanRepository routePlanRepository;
    private final RoutePlanningSessionRepository planningSessionRepository;
    private final IRoutePlanStudentService routePlanStudentService;
    private final ISchoolService schoolService;
    private final ISchoolScheduleService schoolScheduleService;
    private final IDepotService depotService;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;
    private final IRouteGeometryService routeGeometryService;
    private final IRouteStopService routeStopService;
    private final IRouteDispatchService routeDispatchService;
    private final RouteStopFactory routeStopFactory;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;
    private final IRoutePlanningIssueService issueService;

    public RouteServiceImpl(RoutePlanRepository routePlanRepository,
                            RoutePlanningSessionRepository planningSessionRepository,
                            IRoutePlanStudentService routePlanStudentService,
                            ISchoolService schoolService,
                            ISchoolScheduleService schoolScheduleService,
                            IDepotService depotService,
                            IAuditLogService auditLogService,
                            ICodeGeneratorService codeGeneratorService,
                            IRouteGeometryService routeGeometryService,
                            IRouteStopService routeStopService,
                            IRouteDispatchService routeDispatchService,
                            RouteStopFactory routeStopFactory,
                            SchoolBusMapper mapper,
                            MessageCommon messageCommon,
                            @org.springframework.context.annotation.Lazy IRoutePlanningIssueService issueService) {
        this.routePlanRepository = routePlanRepository;
        this.planningSessionRepository = planningSessionRepository;
        this.routePlanStudentService = routePlanStudentService;
        this.schoolService = schoolService;
        this.schoolScheduleService = schoolScheduleService;
        this.depotService = depotService;
        this.auditLogService = auditLogService;
        this.codeGeneratorService = codeGeneratorService;
        this.routeGeometryService = routeGeometryService;
        this.routeStopService = routeStopService;
        this.routeDispatchService = routeDispatchService;
        this.routeStopFactory = routeStopFactory;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
        this.issueService = issueService;
    }


    @Override
    protected BaseRepository<RoutePlanEntity, Long> getRepository() {
        return routePlanRepository;
    }

    // ---- Query / CRUD (kept here) ----

    @Override
    public PageResponse<RoutePlanResponse> getRoutes(RoutePlanParamsRequest params, Long tenantId) {
        return PageResponse.from(routePlanRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "routeCode", "routeName", "status",
                        "shiftType", "routeDirection", "school.name"),
                pageable(params, Set.of("id", "routeCode", "routeName", "serviceDate", "status", "createdAt",
                        "updatedAt", "routeDirection"), "serviceDate")),
                mapper::toRoutePlanResponse);
    }

    @Override
    public RoutePlanEntity getRouteEntity(Long id, Long tenantId) {
        return findById(routePlanRepository, id, tenantId);
    }

    @Override
    public RouteDetailResponse getRoute(Long id, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        RouteDetailResponse detail = mapper.toRouteDetailResponse(route,
                routeStopService.findByRoute(id, tenantId),
                routePlanStudentService.findByRoute(id),
                routeDispatchService.findAssignmentEntityByRoute(id, tenantId).orElse(null));

        List<RoutePlanningIssueEntity> activeIssues = issueService.findByRoute(id).stream()
                .filter(i -> !Boolean.TRUE.equals(i.getIsResolved()))
                .toList();

        List<RouteIssueDetailResponse> issueResponses = activeIssues.stream()
                .map(i -> new RouteIssueDetailResponse(
                        i.getIssueType(),
                        i.getSeverity().name(),
                        i.getMessage(),
                        i.getRouteStop() != null ? i.getRouteStop().getId() : null,
                        i.getRouteStop() != null ? i.getRouteStop().getDisplayName() : null,
                        i.getStudent() != null ? i.getStudent().getId() : null,
                        i.getStudent() != null ? i.getStudent().getFullName() : null,
                        RouteIssueDetailResponse.getSuggestedFix(i.getIssueType())
                ))
                .toList();

        List<RouteIssueDetailResponse> blockingIssues = issueResponses.stream()
                .filter(i -> "BLOCKING".equalsIgnoreCase(i.getSeverity()))
                .toList();

        List<RouteIssueDetailResponse> warningIssues = issueResponses.stream()
                .filter(i -> "WARNING".equalsIgnoreCase(i.getSeverity()))
                .toList();

        detail.setIssues(issueResponses);
        detail.setBlockingIssues(blockingIssues);
        detail.setWarningIssues(warningIssues);

        return detail;
    }

    @Override
    public RoutePathResponse getRoutePath(Long routeId, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);

        RoutePathResponse storedPath = routeGeometryService.deserialize(route.getGeometryPath());
        if (storedPath != null && storedPath.getCoordinates() != null && storedPath.getCoordinates().size() >= 2) {
            storedPath.setRouteId(routeId);
            return storedPath;
        }

        RoutePathResponse none = new RoutePathResponse();
        none.setRouteId(routeId);
        none.setProvider("NONE");
        none.setEstimated(Boolean.TRUE);
        none.setFallbackUsed(Boolean.FALSE);
        none.setGeometrySource("NONE");
        none.setDistanceKm(route.getPlannedDistanceKm());
        none.setDurationMin(route.getPlannedDurationMin());
        none.setWarning("Route geometry not yet computed. Call POST /routes/" + routeId + "/compute-path to generate.");
        return none;
    }

    @Override
    public List<RouteStopResponse> getRouteStops(Long routeId, Long tenantId) {
        findById(routePlanRepository, routeId, tenantId);
        return routeStopService.findByRoute(routeId, tenantId)
                .stream()
                .map(mapper::toRouteStopResponse)
                .toList();
    }

    @Override
    public RouteAssignmentResponse getRouteAssignment(Long routeId, Long tenantId) {
        return mapper.toRouteAssignmentResponse(
                routeDispatchService.findAssignmentEntityByRoute(routeId, tenantId).orElse(null));
    }

    @Override
    @Transactional
    public RoutePlanResponse updateRoute(Long id, RoutePlanUpsertRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        // Block edits once a trip has been created or the route is cancelled.
        // IN_PROGRESS / COMPLETED are not valid RoutePlan statuses — they belong
        // to TripExecution. Use TRIP_CREATED as the terminal editing boundary.
        if (route.getStatus() == RouteStatus.TRIP_CREATED
                || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Route.INVALID_STATE));
        }
        route.markUpdated(actor(actorId));
        applyRoute(route, request, tenantId);
        RoutePlanEntity saved = routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", saved.getId(), "UPDATE", "Updated route plan");
        return mapper.toRoutePlanResponse(saved);
    }

    // ---- Delegated to sub-services ----

    @Override
    public RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request, Long tenantId,
            Long actorId) {
        return routeDispatchService.assignRoute(routeId, request, tenantId, actorId);
    }

    @Override
    public RouteAssignmentResponse manualDispatchRoute(Long routeId, ManualDispatchRequest request, Long tenantId,
            Long actorId) {
        return routeDispatchService.manualDispatchRoute(routeId, request, tenantId, actorId);
    }

    @Override
    public List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId,
            Long actorId) {
        return routeStopService.reorderRouteStops(routeId, request, tenantId, actorId);
    }

    @Override
    @Transactional
    public RoutePathResponse computePath(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        List<RouteStopEntity> stops = routeStopService.findByRoute(routeId, tenantId);
        RoutePathResponse result = routeGeometryService.computeAndUpdate(route, stops);
        routeStopService.saveAllRouteStops(stops);
        route.markUpdated(actor(actorId));
        routePlanRepository.save(route);
        if (route.getPlanningSession() != null) {
            refreshSessionDistanceStats(route.getPlanningSession().getId(), tenantId);
        }
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "COMPUTE_PATH", "Manual geometry recompute");
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssignmentHistoryResponse> getAssignmentHistory(Long routeId, Long tenantId) {
        return routeDispatchService.getAssignmentHistory(routeId, tenantId);
    }

    // ---- Manual editing (delegated to RouteStopService) ----

    @Override
    public RouteStopResponse addStop(Long routeId, AddRouteStopRequest request, Long tenantId, Long actorId) {
        return routeStopService.addStop(routeId, request, tenantId, actorId);
    }

    @Override
    public void removeStop(Long routeId, Long stopId, Long tenantId, Long actorId) {
        routeStopService.removeStop(routeId, stopId, tenantId, actorId);
    }

    @Override
    public RoutePlanStudentResponse assignStudentToRoute(Long routeId,
                                                         AddStudentToStopRequest request,
                                                         Long tenantId, Long actorId) {
        return routeStopService.assignStudentToRoute(routeId, request, tenantId, actorId);
    }

    @Override
    public RoutePlanStudentResponse addStudentToStop(Long routeId, Long stopId,
                                                     AddStudentToStopRequest request,
                                                     Long tenantId, Long actorId) {
        return routeStopService.addStudentToStop(routeId, stopId, request, tenantId, actorId);
    }

    @Override
    public void moveStudent(Long sourceRouteId, MoveStudentRequest request, Long tenantId, Long actorId) {
        routeStopService.moveStudent(sourceRouteId, request, tenantId, actorId);
    }

    @Override
    public void removeStudent(Long routeId, Long studentId, Long subscriptionId, Long tenantId, Long actorId) {
        routeStopService.removeStudent(routeId, studentId, subscriptionId, tenantId, actorId);
    }

    // ---- Private helpers (kept for CRUD apply logic) ----

    private void applyRoute(RoutePlanEntity route, RoutePlanUpsertRequest request, Long tenantId) {
        SchoolEntity school = schoolService.getSchool(request.getSchoolId(), tenantId);
        RouteDirection direction = RouteDirection.parse(request.getRouteDirection());
        RouteLocationType startType = RouteLocationType.parse(request.getStartLocationType());
        RouteLocationType endType = RouteLocationType.parse(request.getEndLocationType());

        validateRouteLocationRule(direction, startType, endType);

        route.setSchool(school);
        route.setRouteDirection(direction);
        applyStartLocation(route, school, startType, request, tenantId);
        applyEndLocation(route, school, endType, request, tenantId);
        
        SchoolScheduleEntity schedule = schoolScheduleService.getSchedule(request.getSchoolScheduleId(), tenantId);
        validateSameSchool(school.getId(), schedule.getSchool().getId(), "school schedule");
        
        route.setRouteName(request.getRouteName());
        route.setServiceDate(request.getServiceDate());
        route.setSchoolSchedule(schedule);
        route.setShiftType(ShiftType.parse(schedule.getShiftType()));
        route.setPlanningNotes(request.getPlanningNotes());
        route.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void validateRouteLocationRule(RouteDirection direction, RouteLocationType startType,
            RouteLocationType endType) {
        if (direction == RouteDirection.OUTBOUND && endType != RouteLocationType.SCHOOL) {
            throw new AppException(AppErrorCode.Route.OUTBOUND_MUST_END_SCHOOL, messageCommon.getMessage(AppErrorCode.Route.OUTBOUND_MUST_END_SCHOOL));
        }
        if (direction == RouteDirection.RETURN && startType != RouteLocationType.SCHOOL) {
            throw new AppException(AppErrorCode.Route.RETURN_MUST_START_SCHOOL, messageCommon.getMessage(AppErrorCode.Route.RETURN_MUST_START_SCHOOL));
        }
    }

    private void applyStartLocation(RoutePlanEntity route, SchoolEntity school, RouteLocationType startType,
            RoutePlanUpsertRequest request, Long tenantId) {
        route.setStartLocationType(startType);
        if (startType == RouteLocationType.SCHOOL) {
            validateSameSchool(request.getStartSchoolId(), school.getId(), "start school");
            route.setStartSchool(school);
            route.setStartDepot(null);
            return;
        }
        DepotEntity depot = getRequiredDepot(request.getStartDepotId(), tenantId, "start depot");
        route.setStartSchool(null);
        route.setStartDepot(depot);
    }

    private void applyEndLocation(RoutePlanEntity route, SchoolEntity school, RouteLocationType endType,
            RoutePlanUpsertRequest request, Long tenantId) {
        route.setEndLocationType(endType);
        if (endType == RouteLocationType.SCHOOL) {
            validateSameSchool(request.getEndSchoolId(), school.getId(), "end school");
            route.setEndSchool(school);
            route.setEndDepot(null);
            return;
        }
        DepotEntity depot = getRequiredDepot(request.getEndDepotId(), tenantId, "end depot");
        route.setEndSchool(null);
        route.setEndDepot(depot);
    }

    private DepotEntity getRequiredDepot(Long depotId, Long tenantId, String fieldName) {
        if (depotId == null) {
            throw new AppException(AppErrorCode.Route.DEPOT_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Route.DEPOT_REQUIRED, fieldName));
        }
        return depotService.getDepot(depotId, tenantId);
    }

    private void validateSameSchool(Long requestedSchoolId, Long routeSchoolId, String fieldName) {
        if (requestedSchoolId == null) {
            throw new AppException(AppErrorCode.Route.SCHOOL_REQUIRED,
                    messageCommon.getMessage(AppErrorCode.Route.SCHOOL_REQUIRED, fieldName));
        }
        if (!routeSchoolId.equals(requestedSchoolId)) {
            throw new AppException(AppErrorCode.Route.SCHOOL_MISMATCH,
                    messageCommon.getMessage(AppErrorCode.Route.SCHOOL_MISMATCH, fieldName));
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName) {
        try {
            return Enum.valueOf(enumType, value == null ? "" : value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(AppErrorCode.Route.FIELD_INVALID,
                    messageCommon.getMessage(AppErrorCode.Route.FIELD_INVALID, fieldName, value));
        }
    }

    private void refreshSessionDistanceStats(Long sessionId, Long tenantId) {
        List<RoutePlanEntity> routes = routePlanRepository.findByPlanningSessionIdAndTenantId(sessionId, tenantId);
        double totalDistKm = routes.stream()
                .mapToDouble(r -> r.getPlannedDistanceKm() != null ? r.getPlannedDistanceKm() : 0.0)
                .sum();
        int totalDurMin = routes.stream()
                .mapToInt(r -> r.getPlannedDurationMin() != null ? r.getPlannedDurationMin() : 0)
                .sum();
        planningSessionRepository.findByIdAndTenantIdAndIsDeletedFalse(sessionId, tenantId)
                .ifPresent(session -> {
                    session.setTotalDistanceKm(totalDistKm > 0 ? totalDistKm : null);
                    session.setTotalDurationMin(totalDurMin > 0 ? totalDurMin : null);
                    planningSessionRepository.save(session);
                });
    }

    private Specification<RoutePlanEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }

    private String generateCode(SchoolBusCode code, Long tenantId, Long actorId) {
        return codeGeneratorService.generate(code.sequenceKey(), code.prefix(), tenantId, actorId);
    }

    @Override
    public RoutePlanEntity saveRouteEntity(RoutePlanEntity entity) {
        return routePlanRepository.save(entity);
    }

    @Override
    public List<RoutePlanEntity> findRoutesBySession(Long sessionId, Long tenantId) {
        return routePlanRepository.findByPlanningSessionIdAndTenantId(sessionId, tenantId);
    }

    @Override
    public List<RoutePlanResponse> listRoutesBySession(Long sessionId, Long tenantId) {
        return findRoutesBySession(sessionId, tenantId)
                .stream()
                .map(mapper::toRoutePlanResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoutePlanResponse createRouteInSession(RoutePlanUpsertRequest request, Long sessionId,
                                                   Long tenantId, Long actorId) {
        RoutePlanningSessionEntity planningSession = planningSessionRepository
                .findByIdAndTenantIdAndIsDeletedFalse(sessionId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Planning session not found"));
        if (planningSession.getStatus() == PlanningSessionStatus.PUBLISHED
                || planningSession.getStatus() == PlanningSessionStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Session.FROZEN,
                    messageCommon.getMessage(AppErrorCode.Session.FROZEN, planningSession.getStatus()));
        }

        if (request.getRouteDirection() != null) {
            RouteDirection reqDir = RouteDirection.parse(request.getRouteDirection());
            if (reqDir != planningSession.getRouteDirection()) {
                throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, "Route direction must match planning session direction.");
            }
        } else {
            request.setRouteDirection(planningSession.getRouteDirection().name());
        }

        RoutePlanEntity route = new RoutePlanEntity();
        route.markCreated(tenantId, actor(actorId));
        applyRoute(route, request, tenantId);
        route.setRouteCode(generateCode(SchoolBusCode.ROUTE, tenantId, actorId));
        route.setStatus(RouteStatus.DRAFT);
        route.setPlanningSession(planningSession);
        RoutePlanEntity saved = routePlanRepository.save(route);

        // Validate terminal coordinates before creating geometry
        try {
            routeStopFactory.validateTerminalCoordinates(saved);
        } catch (IllegalStateException ex) {
            throw new AppException(AppErrorCode.Route.DEPOT_REQUIRED, ex.getMessage());
        }

        // Create START_TERMINAL + END_TERMINAL immediately (no middle stops yet)
        List<RouteStopEntity> terminals = routeStopFactory.buildFullStopList(saved, List.of(), tenantId, actor(actorId));
        routeStopService.saveAllRouteStops(terminals);

        log.info("Creating route in session: sessionId={}, routeId={}", sessionId, saved.getId());
        log.info("Initial route stops created: count={}", terminals.size());

        auditLogService.log(tenantId, actorId, "RoutePlan", saved.getId(), "CREATE",
                "Created route in planning session " + sessionId);
        return mapper.toRoutePlanResponse(saved);
    }

    @Override
    @Transactional
    public RoutePlanResponse computeInitialRoutePath(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = routePlanRepository.findByIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Route not found"));
        List<RouteStopEntity> allStops = routeStopService.findByRoute(routeId, tenantId);

        int stopCount = allStops.size();
        if (stopCount < 2) {
            log.warn("Skip initial trace because route has fewer than 2 stops: routeId={}, stopCount={}", routeId, stopCount);
        } else {
            log.info("Computing initial route path after create: routeId={}, stopCount={}", routeId, stopCount);
        }

        routeGeometryService.computeAndUpdate(route, allStops);
        RoutePlanEntity saved = routePlanRepository.save(route);
        return mapper.toRoutePlanResponse(saved);
    }

    @Override
    public long countByTenantAndStatus(Long tenantId, RouteStatus status) {
        return routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, status);
    }
}
