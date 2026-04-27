package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.application.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.application.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.application.dto.request.ManualDispatchRequest;
import serp.project.school_bus_service.application.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.RouteAttendanceManifestItemResponse;
import serp.project.school_bus_service.application.dto.response.RouteAttendanceManifestResponse;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.application.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.application.dto.response.RoutePathResponse;
import serp.project.school_bus_service.application.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IDepotService;
import serp.project.school_bus_service.core.service.IRouteDispatchService;
import serp.project.school_bus_service.core.service.IRouteLifecycleService;
import serp.project.school_bus_service.core.service.IRoutePathService;
import serp.project.school_bus_service.core.service.IRouteService;
import serp.project.school_bus_service.core.service.IRouteStopService;
import serp.project.school_bus_service.core.service.ISchoolService;
import serp.project.school_bus_service.core.service.IStudentSubscriptionService;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteLocationType;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;
import serp.project.school_bus_service.infrastructure.store.model.DepotEntity;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentSubscriptionEntity;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.ShiftType;
import serp.project.school_bus_service.infrastructure.store.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RouteStopRepository;
import serp.project.school_bus_service.infrastructure.store.repository.AttendanceRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RequestStudentRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Route service facade. Retains CRUD, query, and manifest logic directly.
 * Delegates stop operations, dispatch, and lifecycle to dedicated services.
 */
@Service
@RequiredArgsConstructor
public class RouteServiceImpl extends AbstractBaseService<RoutePlanEntity, Long> implements IRouteService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final RequestStudentRepository requestStudentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ISchoolService schoolService;
    private final IDepotService depotService;
    private final IStudentSubscriptionService subscriptionService;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;
    private final IRoutePathService routePathService;
    private final IRouteStopService routeStopService;
    private final IRouteDispatchService routeDispatchService;
    private final IRouteLifecycleService routeLifecycleService;
    private final SchoolBusMapper mapper;

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
        return mapper.toRouteDetailResponse(route,
                routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(id, tenantId),
                routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(id, tenantId).orElse(null));
    }

    @Override
    public RoutePathResponse getRoutePath(Long routeId, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        List<RouteStopEntity> stops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);

        RoutePathResponse storedPath = routePathService.deserialize(route.getGeometryPath());
        if (storedPath != null && storedPath.getCoordinates() != null && storedPath.getCoordinates().size() >= 2) {
            storedPath.setRouteId(routeId);
            return storedPath;
        }

        RoutePathResponse fallback = new RoutePathResponse();
        fallback.setRouteId(routeId);
        fallback.setProvider("ESTIMATED_LINE");
        fallback.setEstimated(Boolean.TRUE);
        fallback.setDistanceKm(route.getPlannedDistanceKm());
        fallback.setDurationMin(route.getPlannedDurationMin());
        return fallback;
    }

    @Override
    public RouteAttendanceManifestResponse getAttendanceManifest(Long routeId, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        List<RequestStudentEntity> approvedStudents = requestStudentRepository
                .findApprovedManifestBySchoolAndServiceDate(
                        route.getSchool().getId(), route.getServiceDate(), tenantId, RequestStatus.APPROVED);
        Map<Long, StudentSubscriptionEntity> manifestStudents = new LinkedHashMap<>();
        for (StudentSubscriptionEntity subscription : subscriptionService.findEligibleSubscriptions(
                route.getSchool().getId(), route.getRouteDirection(), route.getServiceDate(), tenantId)) {
            boolean isApproved = approvedStudents.stream()
                    .anyMatch(rs -> rs.getStudent().getId().equals(subscription.getStudent().getId()));
            if (isApproved) {
                manifestStudents.put(subscription.getStudent().getId(), subscription);
            }
        }
        Map<Long, AttendanceEntity> latestAttendances = new LinkedHashMap<>();
        for (AttendanceEntity attendance : attendanceRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(routeId, tenantId)) {
            latestAttendances.putIfAbsent(attendance.getStudent().getId(), attendance);
        }

        RouteAttendanceManifestResponse response = new RouteAttendanceManifestResponse();
        response.setRoute(mapper.toRoutePlanResponse(route));
        response.setAssignment(mapper.toRouteAssignmentResponse(
                routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId).orElse(null)));
        response.setStudents(manifestStudents.values().stream()
                .map(subscription -> toManifestItemResponse(route, subscription,
                        latestAttendances.get(subscription.getStudent().getId())))
                .toList());
        return response;
    }

    @Override
    public List<RouteStopResponse> getRouteStops(Long routeId, Long tenantId) {
        findById(routePlanRepository, routeId, tenantId);
        return routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId)
                .stream()
                .map(mapper::toRouteStopResponse)
                .toList();
    }

    @Override
    public RouteAssignmentResponse getRouteAssignment(Long routeId, Long tenantId) {
        return mapper.toRouteAssignmentResponse(
                routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId).orElse(null));
    }

    @Override
    @Transactional
    public RoutePlanResponse createRoute(RoutePlanUpsertRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity route = new RoutePlanEntity();
        route.markCreated(tenantId, actor(actorId));
        applyRoute(route, request, tenantId);
        route.setRouteCode(generateCode(SchoolBusCode.ROUTE, tenantId, actorId));
        route.setStatus(RouteStatus.DRAFT);
        RoutePlanEntity saved = routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", saved.getId(), "CREATE", "Created route plan");
        return mapper.toRoutePlanResponse(saved);
    }

    @Override
    @Transactional
    public RoutePlanResponse updateRoute(Long id, RoutePlanUpsertRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }
        route.markUpdated(actor(actorId));
        applyRoute(route, request, tenantId);
        RoutePlanEntity saved = routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", saved.getId(), "UPDATE", "Updated route plan");
        return mapper.toRoutePlanResponse(saved);
    }

    // ---- Delegated to sub-services ----

    @Override
    public List<RouteStopResponse> generateGreedyPlan(Long routeId, Long tenantId, Long actorId) {
        return routeStopService.generateGreedyPlan(routeId, tenantId, actorId);
    }

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
    public RoutePlanResponse startRoute(Long routeId, Long tenantId, Long actorId) {
        return routeLifecycleService.startRoute(routeId, tenantId, actorId);
    }

    @Override
    public RoutePlanResponse completeRoute(Long routeId, Long tenantId, Long actorId) {
        return routeLifecycleService.completeRoute(routeId, tenantId, actorId);
    }

    @Override
    public RoutePathResponse computePath(Long routeId, Long tenantId, Long actorId) {
        return routeStopService.computePath(routeId, tenantId, actorId);
    }

    // ---- Private helpers (kept for CRUD apply logic) ----

    private void applyRoute(RoutePlanEntity route, RoutePlanUpsertRequest request, Long tenantId) {
        SchoolEntity school = schoolService.getSchool(request.getSchoolId(), tenantId);
        RouteDirection direction = parseEnum(RouteDirection.class, request.getRouteDirection(), "route direction");
        RouteLocationType startType = parseEnum(RouteLocationType.class, request.getStartLocationType(),
                "start location type");
        RouteLocationType endType = parseEnum(RouteLocationType.class, request.getEndLocationType(),
                "end location type");

        validateRouteLocationRule(direction, startType, endType);

        route.setSchool(school);
        route.setRouteDirection(direction);
        applyStartLocation(route, school, startType, request, tenantId);
        applyEndLocation(route, school, endType, request, tenantId);
        route.setRouteName(request.getRouteName());
        route.setServiceDate(request.getServiceDate());
        route.setShiftType(parseEnum(ShiftType.class, request.getShiftType(), "shift type"));
        route.setPlanningNotes(request.getPlanningNotes());
        route.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void validateRouteLocationRule(RouteDirection direction, RouteLocationType startType,
            RouteLocationType endType) {
        if (direction == RouteDirection.OUTBOUND && endType != RouteLocationType.SCHOOL) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Outbound routes must end at the school");
        }
        if (direction == RouteDirection.RETURN && startType != RouteLocationType.SCHOOL) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Return routes must start at the school");
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
            throw new AppException(AppErrorCode.INVALID_REQUEST,
                    String.format("%s is required when location type is DEPOT", fieldName));
        }
        return depotService.getDepot(depotId, tenantId);
    }

    private void validateSameSchool(Long requestedSchoolId, Long routeSchoolId, String fieldName) {
        if (requestedSchoolId == null) {
            throw new AppException(AppErrorCode.INVALID_REQUEST,
                    String.format("%s is required when location type is SCHOOL", fieldName));
        }
        if (!routeSchoolId.equals(requestedSchoolId)) {
            throw new AppException(AppErrorCode.INVALID_REQUEST,
                    String.format("%s must match the route school in v1", fieldName));
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName) {
        try {
            return Enum.valueOf(enumType, value == null ? "" : value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new AppException(AppErrorCode.INVALID_REQUEST,
                    String.format("Invalid %s: %s", fieldName, value));
        }
    }

    private RouteAttendanceManifestItemResponse toManifestItemResponse(RoutePlanEntity route,
            StudentSubscriptionEntity subscription, AttendanceEntity latestAttendance) {
        RouteAttendanceManifestItemResponse response = new RouteAttendanceManifestItemResponse();
        response.setStudentId(subscription.getStudent().getId());
        response.setStudentName(subscription.getStudent().getFullName());
        var point = route.getRouteDirection() == RouteDirection.RETURN
                ? subscription.getDropoffPoint()
                : subscription.getPickupPoint();
        response.setPickupPointId(point == null ? null : point.getId());
        response.setPickupPointName(point == null ? null : point.getName());
        if (latestAttendance != null) {
            response.setLatestAttendanceType(latestAttendance.getAttendanceType().name());
            response.setLatestAttendanceStatus(latestAttendance.getStatus().name());
            response.setLatestRecordedAt(latestAttendance.getRecordedAt());
        }
        return response;
    }

    private Specification<RoutePlanEntity> spec(Long tenantId, String keyword, String... fields) {
        return BaseSpecification.tenantActiveWithKeyword(tenantId, keyword, fields);
    }

    private Pageable pageable(
            serp.project.school_bus_service.application.dto.request.BaseParamsRequest params,
            Set<String> allowedSorts,
            String defaultSortBy) {
        return PageableUtils.from(params, allowedSorts, defaultSortBy);
    }

    private String generateCode(SchoolBusCode code, Long tenantId, Long actorId) {
        return codeGeneratorService.generate(code.sequenceKey(), code.prefix(), tenantId, actorId);
    }
}
