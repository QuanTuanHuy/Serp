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
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.service.IDepotService;
import serp.project.school_bus_service.service.IRouteDispatchService;
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

import java.util.List;
import java.util.Set;

@Service
public class RouteServiceImpl extends AbstractBaseService<RoutePlanEntity, Long> implements IRouteService {

    private static final Logger log = LoggerFactory.getLogger(RouteServiceImpl.class);

    private final RoutePlanRepository routePlanRepository;
    private final RoutePlanningSessionRepository planningSessionRepository;
    private final IRoutePlanStudentService routePlanStudentService;
    private final ISchoolService schoolService;
    private final ISchoolScheduleService schoolScheduleService;
    private final IDepotService depotService;
    private final ICodeGeneratorService codeGeneratorService;
    private final IRouteStopService routeStopService;
    private final IRouteDispatchService routeDispatchService;
    private final SchoolBusMapper mapper;
    private final MessageCommon messageCommon;

    public RouteServiceImpl(RoutePlanRepository routePlanRepository,
                            RoutePlanningSessionRepository planningSessionRepository,
                            IRoutePlanStudentService routePlanStudentService,
                            ISchoolService schoolService,
                            ISchoolScheduleService schoolScheduleService,
                            IDepotService depotService,
                            ICodeGeneratorService codeGeneratorService,
                            IRouteStopService routeStopService,
                            IRouteDispatchService routeDispatchService,
                            SchoolBusMapper mapper,
                            MessageCommon messageCommon) {
        this.routePlanRepository = routePlanRepository;
        this.planningSessionRepository = planningSessionRepository;
        this.routePlanStudentService = routePlanStudentService;
        this.schoolService = schoolService;
        this.schoolScheduleService = schoolScheduleService;
        this.depotService = depotService;
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
        return mapper.toRouteDetailResponse(route,
                routeStopService.findByRoute(id, tenantId),
                routePlanStudentService.findByRoute(id),
                routeDispatchService.findAssignmentEntityByRoute(id, tenantId).orElse(null));
    }

    @Override
    public RoutePathResponse getRoutePath(Long id, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        RoutePathResponse response = new RoutePathResponse();
        response.setRouteId(route.getId());
        response.setDistanceKm(route.getPlannedDistanceKm());
        response.setDurationMin(route.getPlannedDurationMin());

        String geometryPath = route.getGeometryPath();
        if (geometryPath != null && !geometryPath.isBlank()) {
            response.setGeometrySource("ROAD_NETWORK");
            response.setCoordinates(parseGeometryPath(geometryPath));
        } else {
            response.setGeometrySource("NONE");
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
        applyRoute(route, request, tenantId);
        RoutePlanEntity saved = routePlanRepository.save(route);
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

    // ---- Private helpers ----

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

    private Specification<RoutePlanEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(BaseParamsRequest params, Set<String> allowedSorts, String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
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
                throw new AppException(AppErrorCode.REQUEST_VALIDATION_FAILED,
                        "Route direction must match planning session direction.");
            }
        } else {
            request.setRouteDirection(planningSession.getRouteDirection().name());
        }

        RoutePlanEntity route = new RoutePlanEntity();
        route.markCreated(tenantId, actor(actorId));
        applyRoute(route, request, tenantId);
        route.setRouteCode(codeGeneratorService.generate(
                SchoolBusCode.ROUTE.sequenceKey(), SchoolBusCode.ROUTE.prefix(), tenantId, actorId));
        route.setStatus(RouteStatus.DRAFT);
        route.setPlanningSession(planningSession);
        RoutePlanEntity saved = routePlanRepository.save(route);

        log.info("Created route in session: sessionId={}, routeId={}", sessionId, saved.getId());
        return mapper.toRoutePlanResponse(saved);
    }

    @Override
    public long countByTenantAndStatus(Long tenantId, RouteStatus status) {
        return routePlanRepository.countByTenantIdAndStatusAndIsDeletedFalse(tenantId, status);
    }
}
