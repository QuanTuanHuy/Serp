package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.response.RoutePlanResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IRouteLifecycleService;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentEntity;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.TripHistoryEntity;
import serp.project.school_bus_service.infrastructure.store.repository.RouteAssignmentRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.infrastructure.store.repository.TripHistoryRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RouteLifecycleServiceImpl extends AbstractBaseService<RoutePlanEntity, Long>
        implements IRouteLifecycleService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteAssignmentRepository routeAssignmentRepository;
    private final TripHistoryRepository tripHistoryRepository;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<RoutePlanEntity, Long> getRepository() {
        return routePlanRepository;
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

        RouteAssignmentEntity assignment = routeAssignmentRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
                .orElse(null);
        TripHistoryEntity tripHistory = tripHistoryRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalse(routeId, tenantId)
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

        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "COMPLETE",
                "Completed route and wrote trip history");
        return mapper.toRoutePlanResponse(savedRoute);
    }
}
