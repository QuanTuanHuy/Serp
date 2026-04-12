package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.params.RoutePlanParamsRequest;
import serp.project.school_bus_service.application.dto.request.RouteAssignmentRequest;
import serp.project.school_bus_service.application.dto.request.RoutePlanUpsertRequest;
import serp.project.school_bus_service.application.dto.response.PageResponse;
import serp.project.school_bus_service.application.dto.response.RouteAttendanceManifestItemResponse;
import serp.project.school_bus_service.application.dto.response.RouteAttendanceManifestResponse;
import serp.project.school_bus_service.application.dto.response.RouteAssignmentResponse;
import serp.project.school_bus_service.application.dto.response.RouteDetailResponse;
import serp.project.school_bus_service.application.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;
import serp.project.school_bus_service.enums.RequestStatus;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.ICodeGeneratorService;
import serp.project.school_bus_service.core.service.IMasterDataService;
import serp.project.school_bus_service.core.service.IRouteService;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;
import serp.project.school_bus_service.infrastructure.store.model.RequestStudentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripHistoryEntity;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.enums.ShiftType;
import serp.project.school_bus_service.infrastructure.store.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RouteStopRepository;
import serp.project.school_bus_service.infrastructure.store.repository.AttendanceRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RequestStudentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripHistoryRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.code.SchoolBusCode;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;
import serp.project.school_bus_service.kernel.shared.pagination.PageableUtils;
import serp.project.school_bus_service.infrastructure.store.specification.BaseSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RouteServiceImpl extends AbstractBaseService<RoutePlanEntity, Long> implements IRouteService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final RequestStudentRepository requestStudentRepository;
    private final AttendanceRepository attendanceRepository;
    private final TripHistoryRepository tripHistoryRepository;
    private final IMasterDataService masterDataService;
    private final RoutePlanningService routePlanningService;
    private final IAuditLogService auditLogService;
    private final ICodeGeneratorService codeGeneratorService;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<RoutePlanEntity, Long> getRepository() {
        return routePlanRepository;
    }

    @Override
    public PageResponse<RoutePlanResponse> getRoutes(RoutePlanParamsRequest params, Long tenantId) {
        return PageResponse.from(routePlanRepository.findAll(
                spec(tenantId, params == null ? null : params.getKeyword(), "routeCode", "routeName", "status",
                        "shiftType", "school.name"),
                pageable(params, Set.of("id", "routeCode", "routeName", "serviceDate", "status", "createdAt",
                        "updatedAt"), "serviceDate")),
                mapper::toRoutePlanResponse);
    }

    @Override
    public RouteDetailResponse getRoute(Long id, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, id, tenantId);
        return mapper.toRouteDetailResponse(route,
                routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(id, tenantId),
                routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(id, tenantId).orElse(null));
    }

    @Override
    public RouteAttendanceManifestResponse getAttendanceManifest(Long routeId, Long tenantId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        List<RequestStudentEntity> requestStudents = requestStudentRepository.findApprovedManifestBySchoolAndServiceDate(
                route.getSchool().getId(),
                route.getServiceDate(),
                tenantId,
                RequestStatus.APPROVED);
        List<AttendanceEntity> attendances = attendanceRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(
                routeId,
                tenantId);

        Map<Long, RequestStudentEntity> manifestStudents = new LinkedHashMap<>();
        for (RequestStudentEntity requestStudent : requestStudents) {
            manifestStudents.putIfAbsent(requestStudent.getStudent().getId(), requestStudent);
        }

        Map<Long, AttendanceEntity> latestAttendances = new LinkedHashMap<>();
        for (AttendanceEntity attendance : attendances) {
            latestAttendances.putIfAbsent(attendance.getStudent().getId(), attendance);
        }

        RouteAttendanceManifestResponse response = new RouteAttendanceManifestResponse();
        response.setRoute(mapper.toRoutePlanResponse(route));
        response.setAssignment(mapper.toRouteAssignmentResponse(
                routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId).orElse(null)));
        response.setStudents(manifestStudents.values().stream()
                .map(requestStudent -> toManifestItemResponse(requestStudent, latestAttendances.get(requestStudent.getStudent().getId())))
                .toList());
        return response;
    }

    @Override
    public List<RouteStopResponse> getRouteStops(Long routeId, Long tenantId) {
        findById(routePlanRepository, routeId, tenantId);
        return routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId).stream()
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

    @Override
    @Transactional
    public List<RouteStopResponse> generateGreedyPlan(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        routeStopRepository.softDeleteByRouteIdAndTenantId(routeId, tenantId, actor(actorId));
        List<RouteStopEntity> stops = routePlanningService.generateGreedyStops(route, tenantId);
        routeStopRepository.saveAll(stops);
        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.PLANNED);
        route.setPlannedDurationMin(stops.size() * 10);
        route.setPlannedDistanceKm((double) stops.size() * 2.5d);
        routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "PLAN", "Generated greedy route stops");
        return routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId).stream()
                .map(mapper::toRouteStopResponse)
                .toList();
    }

    @Override
    @Transactional
    public RouteAssignmentResponse assignRoute(Long routeId, RouteAssignmentRequest request, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.CANCELLED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        validateAssignmentConflict(routeId, route.getServiceDate(), request, tenantId);

        RouteAssignmentEntity assignment = routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseGet(RouteAssignmentEntity::new);
        if (assignment.getId() == null) {
            assignment.markCreated(tenantId, actor(actorId));
        } else {
            assignment.markUpdated(actor(actorId));
        }
        assignment.setRoute(route);
        assignment.setBus(masterDataService.getBus(request.getBusId(), tenantId));
        assignment.setDriver(masterDataService.getDriver(request.getDriverId(), tenantId));
        assignment.setAttendant(request.getAttendantId() == null ? null : masterDataService.getAttendant(request.getAttendantId(), tenantId));
        assignment.setAssignedAt(LocalDateTime.now());
        RouteAssignmentEntity saved = routeAssignmentRepository.save(assignment);
        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.ASSIGNED);
        routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "ASSIGN", "Assigned route resources");
        return mapper.toRouteAssignmentResponse(saved);
    }

    @Override
    @Transactional
    public RoutePlanResponse startRoute(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        if (route.getStatus() != RouteStatus.ASSIGNED && route.getStatus() != RouteStatus.PLANNED) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }
        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.IN_PROGRESS);
        route.setStartedAt(LocalDateTime.now());
        RoutePlanEntity saved = routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "START", "Started route");
        return mapper.toRoutePlanResponse(saved);
    }

    @Override
    @Transactional
    public RoutePlanResponse completeRoute(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        if (route.getStatus() != RouteStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        route.markUpdated(actor(actorId));
        route.setStatus(RouteStatus.COMPLETED);
        route.setCompletedAt(LocalDateTime.now());
        RoutePlanEntity savedRoute = routePlanRepository.save(route);

        RouteAssignmentEntity assignment = routeAssignmentRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElse(null);
        TripHistoryEntity tripHistory = tripHistoryRepository.findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElseGet(TripHistoryEntity::new);
        if (tripHistory.getId() == null) {
            tripHistory.markCreated(tenantId, actor(actorId));
        } else {
            tripHistory.markUpdated(actor(actorId));
        }
        tripHistory.setRoute(route);
        tripHistory.setRouteCode(route.getRouteCode());
        tripHistory.setServiceDate(route.getServiceDate());
        tripHistory.setStatus(route.getStatus().name());
        tripHistory.setStartedAt(route.getStartedAt());
        tripHistory.setCompletedAt(route.getCompletedAt());
        if (assignment != null) {
            tripHistory.setBus(assignment.getBus());
            tripHistory.setDriver(assignment.getDriver());
            tripHistory.setAttendant(assignment.getAttendant());
        }
        tripHistoryRepository.save(tripHistory);

        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "COMPLETE", "Completed route and wrote trip history");
        return mapper.toRoutePlanResponse(savedRoute);
    }

    private void applyRoute(RoutePlanEntity route, RoutePlanUpsertRequest request, Long tenantId) {
        route.setSchool(masterDataService.getSchool(request.getSchoolId(), tenantId));
        route.setRouteName(request.getRouteName());
        route.setServiceDate(request.getServiceDate());
        route.setShiftType(ShiftType.valueOf(request.getShiftType().toUpperCase()));
        route.setPlanningNotes(request.getPlanningNotes());
        route.setIsActive(request.resolveIsActive(Boolean.TRUE));
    }

    private void validateAssignmentConflict(Long routeId, LocalDate serviceDate, RouteAssignmentRequest request, Long tenantId) {
        validateRouteResourceConflict(routeId, serviceDate,
                routeAssignmentRepository.findByBusIdAndTenantIdAndIsDeletedFalse(request.getBusId(), tenantId));
        validateRouteResourceConflict(routeId, serviceDate,
                routeAssignmentRepository.findByDriverIdAndTenantIdAndIsDeletedFalse(request.getDriverId(), tenantId));
        if (request.getAttendantId() != null) {
            validateRouteResourceConflict(routeId, serviceDate,
                    routeAssignmentRepository.findByAttendantIdAndTenantIdAndIsDeletedFalse(request.getAttendantId(), tenantId));
        }
    }

    private void validateRouteResourceConflict(Long routeId, LocalDate serviceDate, List<RouteAssignmentEntity> assignments) {
        boolean hasConflict = assignments.stream()
                .map(RouteAssignmentEntity::getRoute)
                .filter(route -> !route.getId().equals(routeId))
                .filter(route -> route.getServiceDate().equals(serviceDate))
                .anyMatch(route -> route.getStatus() != RouteStatus.CANCELLED && route.getStatus() != RouteStatus.COMPLETED);
        if (hasConflict) {
            throw new AppException(AppErrorCode.CONFLICT);
        }
    }

    private RouteAttendanceManifestItemResponse toManifestItemResponse(RequestStudentEntity requestStudent,
            AttendanceEntity latestAttendance) {
        RouteAttendanceManifestItemResponse response = new RouteAttendanceManifestItemResponse();
        response.setStudentId(requestStudent.getStudent().getId());
        response.setStudentName(requestStudent.getStudent().getFullName());
        response.setPickupPointId(requestStudent.getPickupPoint() == null ? null : requestStudent.getPickupPoint().getId());
        response.setPickupPointName(
                requestStudent.getPickupPoint() == null ? null : requestStudent.getPickupPoint().getName());
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
