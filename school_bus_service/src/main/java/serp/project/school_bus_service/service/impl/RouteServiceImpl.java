package serp.project.school_bus_service.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.dto.request.*;
import serp.project.school_bus_service.dto.response.PageResponse;
import serp.project.school_bus_service.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.dto.response.RouteMapResponse;
import serp.project.school_bus_service.dto.response.RoutePathResponse;
import serp.project.school_bus_service.dto.response.RoutePlanListItemResponse;
import serp.project.school_bus_service.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.dto.response.RoutePlanStudentResponse;
import serp.project.school_bus_service.dto.response.RouteDispatchSummaryResponse;
import serp.project.school_bus_service.dto.response.RouteStopResponse;
import serp.project.school_bus_service.entity.RoutePlanStudentEntity;
import serp.project.school_bus_service.service.IRoutePlanStudentService;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IBusService;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.service.IRouteDispatchService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.ISchoolService;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteGeometrySource;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.mapper.SchoolBusMapper;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.RouteAssignmentEntity;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.repository.RoutePlanRepository;
import serp.project.school_bus_service.repository.RoutePlanningSessionRepository;
import serp.project.school_bus_service.repository.RouteStopRepository;
import serp.project.school_bus_service.repository.projection.RouteAssignmentSummaryProjection;
import serp.project.school_bus_service.repository.projection.RouteStopCountProjection;
import serp.project.school_bus_service.repository.projection.RouteTerminalStopProjection;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.shared.code.SchoolBusCode;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;
import serp.project.school_bus_service.shared.pagination.PageableUtils;
import serp.project.school_bus_service.shared.base.specification.BaseSpecification;

import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Service
public class RouteServiceImpl extends AbstractBaseService<RoutePlanEntity, Long> implements IRouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);

    private final RoutePlanRepository routePlanRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final RoutePlanningSessionRepository planningSessionRepository;
    private final IRoutePlanStudentService routePlanStudentService;
    private final ISchoolService schoolService;
    private final IDepotService depotService;
    private final IBusService busService;
    private final ICodeGeneratorService codeGeneratorService;
    private final IRouteStopService routeStopService;
    private final IRouteDispatchService routeDispatchService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;

    public RouteServiceImpl(RoutePlanRepository routePlanRepository,
                            RouteStopRepository routeStopRepository,
                            RouteAssignmentRepository routeAssignmentRepository,
                            RoutePlanningSessionRepository planningSessionRepository,
                            IRoutePlanStudentService routePlanStudentService,
                            ISchoolService schoolService,
                            IDepotService depotService,
                            IBusService busService,
                            ICodeGeneratorService codeGeneratorService,
                            IRouteStopService routeStopService,
                            IRouteDispatchService routeDispatchService,
                            SchoolBusMapper mapper,
                            MessageCommon messageCommon) {
        this.routePlanRepository = routePlanRepository;
        this.routeStopRepository = routeStopRepository;
        this.routeAssignmentRepository = routeAssignmentRepository;
        this.planningSessionRepository = planningSessionRepository;
        this.routePlanStudentService = routePlanStudentService;
        this.schoolService = schoolService;
        this.depotService = depotService;
        this.busService = busService;
        this.codeGeneratorService = codeGeneratorService;
        this.routeStopService = routeStopService;
        this.routeDispatchService = routeDispatchService;
        this.mapper = mapper;
        this.messageCommon = messageCommon;
    }

    @Override
    protected BaseRepository<RoutePlanEntity, Long> getRepository() {
        return routePlanRepository;
    }

    @Override
    public PageResponse<RoutePlanListItemResponse> getRoutes(RoutePlanParamsRequest params, Long tenantId) {
        Page<RoutePlanListItemResponse> page = routePlanRepository.findRouteListItems(
                tenantId,
                keywordPattern(params == null ? null : params.getKeyword()),
                pageable(params, Set.of("id", "routeCode", "routeName", "status", "createdAt",
                        "updatedAt", "lastModifiedDate"), "lastModifiedDate"));
        enrichRouteListItems(page.getContent(), tenantId);
        return PageResponse.from(page, route -> route);
    }

    @Override
    @Transactional(readOnly = true)
    public RouteDispatchSummaryResponse getDispatchSummary(Long tenantId) {
        var summary = routePlanRepository.getDispatchSummary(tenantId);
        return new RouteDispatchSummaryResponse(
                value(summary.getTotalRoutes()),
                value(summary.getPlannedRoutes()),
                value(summary.getTripCreatedRoutes()));
    }

    @Override
    public RoutePlanEntity getRouteEntity(Long id, Long tenantId) {
        return findById(routePlanRepository, id, tenantId);
    }

    @Override
    public RouteDetailResponse getRoute(Long id, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        populateRouteDerivedFields(route, tenantId);
        RouteDetailResponse response = mapper.toRouteDetailResponse(route,
                routeStopService.findByRoute(id, tenantId),
                routePlanStudentService.findByRoute(id),
                routeDispatchService.findAssignmentEntityByRoute(id, tenantId).orElse(null));
        response.setRoute(toRoutePlanResponse(route, tenantId));
        return response;
    }

    @Override
    public RouteMapResponse getRouteMap(Long id, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        populateRouteDerivedFields(route, tenantId);

        RouteMapResponse response = new RouteMapResponse();
        response.setRoute(toRoutePlanResponse(route, tenantId));
        response.setStops(routeStopService.findByRoute(id, tenantId).stream()
                .map(mapper::toRouteStopResponse)
                .toList());
        response.setAssignment(routeDispatchService.findAssignmentEntityByRoute(id, tenantId)
                .map(mapper::toRouteAssignmentResponse)
                .orElse(null));
        response.setPath(buildRoutePath(route));
        return response;
    }

    @Override
    public RoutePathResponse getRoutePath(Long id, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        return buildRoutePath(route);
    }

    private RoutePathResponse buildRoutePath(RoutePlanEntity route) {
        RoutePathResponse response = new RoutePathResponse();
        response.setRouteId(route.getId());
        response.setDistanceKm(route.getPlannedDistanceKm());
        response.setDurationMin(route.getPlannedDurationMin());

        String geometryPath = route.getGeometryPath();
        RouteGeometrySource geometrySource = route.getGeometrySource() != null
                ? route.getGeometrySource() : RouteGeometrySource.UNKNOWN;
        response.setGeometrySource(geometrySource.name());
        response.setFallbackUsed(geometrySource == RouteGeometrySource.HAVERSINE_FALLBACK);
        if (geometryPath != null && !geometryPath.isBlank()) {
            response.setCoordinates(parseGeometryPath(geometryPath));
        } else {
            response.setCoordinates(List.of());
        }
        return response;
    }

    /**
     * Parse GeoJSON coordinate array [[lng,lat],...] into list of {latitude, longitude} objects.
     * OSRM returns [lng, lat] but Leaflet needs [lat, lng], so we swap here.
     */
    private List<RoutePathResponse.Coordinate> parseGeometryPath(String geometryJson) {
        try {
            List<RoutePathResponse.Coordinate> coords = new java.util.ArrayList<>();
            // Simple parse: remove outer brackets, split by ],[
            String inner = geometryJson.trim();
            if (inner.startsWith("[")) inner = inner.substring(1);
            if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);
            // Now inner is like: [lng,lat],[lng,lat],...
            String[] pairs = inner.split("\\],\\[");
            for (String pair : pairs) {
                String clean = pair.replace("[", "").replace("]", "").trim();
                String[] parts = clean.split(",");
                if (parts.length >= 2) {
                    double lng = Double.parseDouble(parts[0].trim());
                    double lat = Double.parseDouble(parts[1].trim());
                    coords.add(new RoutePathResponse.Coordinate(lat, lng)); // swap: lat first for frontend
                }
            }
            return coords;
        } catch (Exception e) {
            return List.of();
        }
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
        if (route.getStatus() == RouteStatus.TRIP_CREATED
                || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.Route.INVALID_STATE, messageCommon.getMessage(AppErrorCode.Route.INVALID_STATE));
        }
        route.markUpdated(actor(actorId));
        if (route.getPlanningSession() != null && !request.getSchoolId().equals(route.getPlanningSession().getSchool().getId())) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, "School ID cannot be changed and must match the planning session's school.");
        }
        Long currentBusId = routeDispatchService.findAssignmentEntityByRoute(id, tenantId)
                .map(assignment -> assignment.getBus().getId())
                .orElseGet(() -> route.getSelectedBus() == null ? null : route.getSelectedBus().getId());
        applyRoute(route, request, tenantId);
        if (request.getBusId() != null && !request.getBusId().equals(currentBusId)) {
            validateBusCanBeChanged(route, tenantId);
            applySelectedBus(route, request.getBusId(), tenantId, false,
                    route.getPlanningSession() == null ? null : route.getPlanningSession().getId(),
                    route.getId());
        }
        RoutePlanEntity saved = routePlanRepository.save(route);
        if (request.getBusId() != null && !request.getBusId().equals(currentBusId)) {
            routeDispatchService.reserveBusForRoute(saved.getId(), request.getBusId(), tenantId, actorId);
        }
        routeStopService.updateTerminalStops(saved, tenantId, actorId);
        return toRoutePlanResponse(saved, tenantId);
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

    // ---- Private helpers ----

    private String keywordPattern(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return "%" + keyword.trim().toLowerCase() + "%";
    }

    private void enrichRouteListItems(List<RoutePlanListItemResponse> routes, Long tenantId) {
        if (routes == null || routes.isEmpty()) {
            return;
        }
        List<Long> routeIds = routes.stream().map(RoutePlanListItemResponse::getId).toList();
        Map<Long, Integer> stopCounts = new HashMap<>();
        for (RouteStopCountProjection projection : routeStopRepository.countActiveStopsByRouteIds(tenantId, routeIds)) {
            stopCounts.put(projection.getRouteId(), projection.getStopsCount());
        }

        Map<Long, String> startNames = new HashMap<>();
        Map<Long, String> endNames = new HashMap<>();
        for (RouteTerminalStopProjection projection : routeStopRepository.findTerminalStopsByRouteIds(tenantId, routeIds)) {
            if ("START_TERMINAL".equals(projection.getStopPurpose())) {
                startNames.put(projection.getRouteId(), projection.getLocationName());
            }
            if ("END_TERMINAL".equals(projection.getStopPurpose())) {
                endNames.put(projection.getRouteId(), projection.getLocationName());
            }
        }

        Map<Long, RouteAssignmentSummaryProjection> assignments = new HashMap<>();
        for (RouteAssignmentSummaryProjection projection : routeAssignmentRepository.findCurrentSummariesByRouteIds(tenantId, routeIds)) {
            assignments.put(projection.getRouteId(), projection);
        }

        for (RoutePlanListItemResponse route : routes) {
            route.setStopsCount(stopCounts.getOrDefault(route.getId(), 0));
            route.setStartLocationName(startNames.get(route.getId()));
            route.setEndLocationName(endNames.get(route.getId()));
            RouteAssignmentSummaryProjection assignment = assignments.get(route.getId());
            if (assignment != null) {
                route.setBusId(assignment.getBusId());
                route.setBusPlateNumber(assignment.getBusPlateNumber());
                route.setBusName(assignment.getBusPlateNumber());
                route.setBusCapacity(assignment.getBusCapacity());
                route.setBusStatus(assignment.getBusStatus());
                route.setDriverId(assignment.getDriverId());
                route.setDriverName(assignment.getDriverName());
                route.setAttendantId(assignment.getAttendantId());
                route.setAttendantName(assignment.getAttendantName());
            }
        }
    }

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

        route.setRouteName(request.getRouteName());
        route.setServiceDate(request.getServiceDate());
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

    private Specification<RoutePlanEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        BaseParamsRequest sortParams = params;
        Set<String> effectiveAllowedSorts = allowedSorts;
        String mappedSortBy = mapDerivedRouteSort(params == null ? null : params.getSortBy());
        if (mappedSortBy != null) {
            sortParams = copyParamsWithSort(params, mappedSortBy);
            effectiveAllowedSorts = new HashSet<>(allowedSorts);
            effectiveAllowedSorts.add(mappedSortBy);
        }
        return PageableUtils.from(sortParams, effectiveAllowedSorts, defaultSortBy);
    }

    private String mapDerivedRouteSort(String sortBy) {
        if ("schoolId".equals(sortBy) || "schoolName".equals(sortBy)) {
            return "planningSession.school.name";
        }
        if ("serviceDate".equals(sortBy)) {
            return "planningSession.serviceDate";
        }
        if ("routeDirection".equals(sortBy)) {
            return "planningSession.routeDirection";
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
                .map(route -> toRoutePlanResponse(route, tenantId))
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

        if (request.getSchoolId() == null
                || planningSession.getSchool() == null
                || !planningSession.getSchool().getId().equals(request.getSchoolId())) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Route school must match the planning session school.");
        }

        if (request.getRouteDirection() != null) {
            RouteDirection reqDir = RouteDirection.parse(request.getRouteDirection());
            if (reqDir != planningSession.getRouteDirection()) {
                throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                        "Route direction must match planning session direction.");
            }
        } else {
            request.setRouteDirection(planningSession.getRouteDirection().name());
        }

        RoutePlanEntity route = new RoutePlanEntity();
        route.markCreated(tenantId, actor(actorId));
        applyRoute(route, request, tenantId);
        applySelectedBus(route, request.getBusId(), tenantId, true, sessionId, null);
        route.setRouteCode(codeGeneratorService.generate(
                SchoolBusCode.ROUTE.sequenceKey(), SchoolBusCode.ROUTE.prefix(), tenantId, actorId));
        route.setStatus(RouteStatus.DRAFT);
        route.setPlanningSession(planningSession);
        RoutePlanEntity saved = routePlanRepository.save(route);
        routeStopService.updateTerminalStops(saved, tenantId, actorId);
        routeDispatchService.reserveBusForRoute(saved.getId(), request.getBusId(), tenantId, actorId);

        log.info("Created route in session: sessionId={}, routeId={}", sessionId, saved.getId());
        return toRoutePlanResponse(saved, tenantId);
    }

    private void applySelectedBus(RoutePlanEntity route, Long busId, Long tenantId, boolean required,
                                  Long sessionId, Long excludeRouteId) {
        if (busId == null) {
            if (!required) {
                return;
            }
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Please select a bus before creating a route.");
        }
        BusEntity bus = busService.getBus(busId, tenantId);
        if (Boolean.TRUE.equals(bus.getIsDeleted()) || Boolean.FALSE.equals(bus.getIsActive())) {
            throw new AppException(AppErrorCode.Bus.INACTIVE,
                    messageCommon.getMessage(AppErrorCode.Bus.INACTIVE));
        }
        DepotEntity routeDepot = route.getStartDepot() != null ? route.getStartDepot() : route.getEndDepot();
        if (routeDepot == null) {
            throw new AppException(AppErrorCode.Route.DEPOT_REQUIRED,
                    "Please select a depot before creating a route.");
        }
        if (bus.getHomeDepot() == null || !routeDepot.getId().equals(bus.getHomeDepot().getId())) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "The selected bus does not belong to the selected depot.");
        }
        if (sessionId != null && routePlanRepository.existsActiveRouteUsingSelectedBusInSession(
                tenantId, sessionId, busId, excludeRouteId)) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "This bus is already assigned to another route in the current planning session.");
        }
        route.setSelectedBus(bus);
        route.setRequiredCapacity(0);
    }

    private void validateBusCanBeChanged(RoutePlanEntity route, Long tenantId) {
        if (route.getPlannedStudentCount() != null && route.getPlannedStudentCount() > 0) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Cannot change bus when the route already has students.");
        }
        RouteAssignmentEntity assignment = routeDispatchService.findAssignmentEntityByRoute(route.getId(), tenantId)
                .orElse(null);
        if (assignment != null && (assignment.getDriver() != null || assignment.getAttendant() != null)) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "Cannot change bus when staff has already been assigned.");
        }
    }

    private RoutePlanResponse toRoutePlanResponse(RoutePlanEntity route, Long tenantId) {
        populateRouteDerivedFields(route, tenantId);
        RoutePlanResponse response = mapper.toRoutePlanResponse(route);
        response.setStopsCount(routeStopService.findByRoute(route.getId(), tenantId).size());
        RouteAssignmentEntity assignment = routeDispatchService
                .findAssignmentEntityByRoute(route.getId(), tenantId)
                .orElse(null);
        if (assignment != null) {
            if (assignment.getDriver() != null) {
                response.setDriverId(assignment.getDriver().getId());
                response.setDriverName(assignment.getDriver().getFullName());
            }
            if (assignment.getAttendant() != null) {
                response.setAttendantId(assignment.getAttendant().getId());
                response.setAttendantName(assignment.getAttendant().getFullName());
            }
        }
        return response;
    }

    private void populateRouteDerivedFields(RoutePlanEntity route, Long tenantId) {
        if (route == null || route.getId() == null) {
            return;
        }
        routeDispatchService.findAssignmentEntityByRoute(route.getId(), tenantId)
                .ifPresent(assignment -> route.setSelectedBus(assignment.getBus()));
        List<RouteStopEntity> stops = routeStopService.findByRoute(route.getId(), tenantId);
        stops.stream()
                .min(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .ifPresent(stop -> applyRouteEndpoint(route, stop, true));
        stops.stream()
                .max(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .ifPresent(stop -> applyRouteEndpoint(route, stop, false));
    }

    private void applyRouteEndpoint(RoutePlanEntity route, RouteStopEntity stop, boolean start) {
        if (stop == null || stop.getLocationType() == null) {
            return;
        }
        if (start) {
            route.setStartLocationType(stop.getLocationType());
            route.setStartSchool(stop.getSchool());
            route.setStartDepot(stop.getDepot());
        } else {
            route.setEndLocationType(stop.getLocationType());
            route.setEndSchool(stop.getSchool());
            route.setEndDepot(stop.getDepot());
        }
    }
    @Override
    @Transactional
    public void deleteRoute(Long sessionId, Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = getRouteEntity(routeId, tenantId);

        if (route.getPlanningSession() == null || !route.getPlanningSession().getId().equals(sessionId)) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED, "Route does not belong to this planning session.");
        }

        if (route.getStatus() == RouteStatus.PUBLISHED
                || route.getStatus() == RouteStatus.TRIP_CREATED
                || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "This route cannot be deleted because it already has students or assigned staff. Please remove students and assignments before deleting the route.");
        }

        if (route.getStatus() == RouteStatus.ASSIGNED
                || routeDispatchService.findAssignmentEntityByRoute(routeId, tenantId).isPresent()) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "This route cannot be deleted because it already has students or assigned staff. Please remove students and assignments before deleting the route.");
        }

        if ((route.getPlannedStudentCount() != null && route.getPlannedStudentCount() > 0)
                || !routePlanStudentService.findByRoute(routeId).isEmpty()) {
            throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                    "This route cannot be deleted because it already has students or assigned staff. Please remove students and assignments before deleting the route.");
        }

        List<RouteStopEntity> stops = routeStopService.findByRoute(routeId, tenantId);
        for (RouteStopEntity stop : stops) {
            if ((stop.getPlannedBoardingCount() != null && stop.getPlannedBoardingCount() > 0)
                    || (stop.getPlannedDropoffCount() != null && stop.getPlannedDropoffCount() > 0)
                    || (stop.getEstimatedStudentCount() != null && stop.getEstimatedStudentCount() > 0)) {
                throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                        "This route cannot be deleted because it already has students or assigned staff. Please remove students and assignments before deleting the route.");
            }
        }

        // Soft delete stops associated with this route
        if (!stops.isEmpty()) {
            List<Long> stopIds = stops.stream().map(RouteStopEntity::getId).toList();
            routeStopService.softDeleteByIds(stopIds, tenantId, actorId);
        }

        // Soft delete the route itself
        softDeleteById(routeId, tenantId, actorId);
    }

    @Override
    public long countByTenantAndStatus(Long tenantId, RouteStatus status) {
        return routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, status);
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }
}
