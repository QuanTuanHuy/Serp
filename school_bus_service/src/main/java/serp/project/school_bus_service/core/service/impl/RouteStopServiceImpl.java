package serp.project.school_bus_service.core.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.application.dto.request.ReorderStopsRequest;
import serp.project.school_bus_service.application.dto.response.RoutePathCoordinateResponse;
import serp.project.school_bus_service.application.dto.response.RoutePathResponse;
import serp.project.school_bus_service.application.dto.response.RouteStopResponse;
import serp.project.school_bus_service.core.service.IAuditLogService;
import serp.project.school_bus_service.core.service.IRoutePathService;
import serp.project.school_bus_service.core.service.IRoutePlanningService;
import serp.project.school_bus_service.core.service.IRouteStopService;
import serp.project.school_bus_service.enums.RouteStatus;
import serp.project.school_bus_service.infrastructure.store.mapper.SchoolBusMapper;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.infrastructure.store.model.RouteStopEntity;
import serp.project.school_bus_service.infrastructure.store.repository.RoutePlanRepository;
import serp.project.school_bus_service.infrastructure.store.repository.RouteStopRepository;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseService;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.kernel.shared.exception.AppErrorCode;
import serp.project.school_bus_service.kernel.shared.exception.AppException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RouteStopServiceImpl extends AbstractBaseService<RoutePlanEntity, Long>
        implements IRouteStopService {

    private final RoutePlanRepository routePlanRepository;
    private final RouteStopRepository routeStopRepository;
    private final IRoutePlanningService routePlanningService;
    private final IRoutePathService routePathService;
    private final IAuditLogService auditLogService;
    private final SchoolBusMapper mapper;

    @Override
    protected BaseRepository<RoutePlanEntity, Long> getRepository() {
        return routePlanRepository;
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
        route.setPlannedStudentCount(stops.stream()
                .map(RouteStopEntity::getEstimatedStudentCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
        route.setPlannedDurationMin(stops.size() * 10);
        route.setPlannedDistanceKm((double) stops.size() * 2.5d);
        route.setGeometryPath(null);
        routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "PLAN", "Generated greedy route stops");
        return routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId)
                .stream()
                .map(mapper::toRouteStopResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<RouteStopResponse> reorderRouteStops(Long routeId, ReorderStopsRequest request, Long tenantId,
            Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        if (route.getStatus() == RouteStatus.COMPLETED || route.getStatus() == RouteStatus.IN_PROGRESS) {
            throw new AppException(AppErrorCode.INVALID_STATE);
        }

        List<RouteStopEntity> stops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);
        if (stops.size() != request.getOrderedStopIds().size()) {
            throw new AppException(AppErrorCode.INVALID_REQUEST, "Stop count mismatch");
        }

        for (int i = 0; i < request.getOrderedStopIds().size(); i++) {
            Long stopId = request.getOrderedStopIds().get(i);
            RouteStopEntity stop = stops.stream().filter(s -> s.getId().equals(stopId)).findFirst()
                    .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Stop not found"));
            stop.setStopOrder(i);
        }

        routeStopRepository.saveAll(stops);
        route.markUpdated(actor(actorId));
        route.setGeometryPath(null);
        routePlanRepository.save(route);
        auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "REORDER_STOPS", "Reordered route stops");
        return routeStopRepository.findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId)
                .stream()
                .map(mapper::toRouteStopResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoutePathResponse computePath(Long routeId, Long tenantId, Long actorId) {
        RoutePlanEntity route = findById(routePlanRepository, routeId, tenantId);
        List<RouteStopEntity> stops = routeStopRepository
                .findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(routeId, tenantId);
        List<RoutePathCoordinateResponse> waypoints = collectWaypoints(route, stops);
        if (waypoints.size() < 2) {
            RoutePathResponse fallback = buildFallbackPath(route, stops,
                    "Insufficient coordinates for real routing. Using estimated line.");
            route.setGeometryPath(routePathService.serialize(fallback));
            route.markUpdated(actor(actorId));
            routePlanRepository.save(route);
            return fallback;
        }

        try {
            RoutePathResponse computed = routePathService.computePath(routeId, waypoints);
            route.setGeometryPath(routePathService.serialize(computed));
            route.setPlannedDistanceKm(computed.getDistanceKm());
            route.setPlannedDurationMin(computed.getDurationMin());
            route.markUpdated(actor(actorId));
            routePlanRepository.save(route);
            auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "COMPUTE_PATH",
                    "Computed real routing path");
            return computed;
        } catch (Exception exception) {
            RoutePathResponse fallback = buildFallbackPath(route, stops,
                    "Routing provider unavailable. Fallback to estimated path.");
            route.setGeometryPath(routePathService.serialize(fallback));
            route.markUpdated(actor(actorId));
            routePlanRepository.save(route);
            auditLogService.log(tenantId, actorId, "RoutePlan", route.getId(), "COMPUTE_PATH_FALLBACK",
                    "Routing provider unavailable, used fallback path");
            return fallback;
        }
    }

    private RoutePathResponse buildFallbackPath(RoutePlanEntity route, List<RouteStopEntity> stops, String warning) {
        List<RoutePathCoordinateResponse> coordinates = collectWaypoints(route, stops);
        RoutePathResponse response = new RoutePathResponse();
        response.setRouteId(route.getId());
        response.setProvider("ESTIMATED_LINE");
        response.setEstimated(Boolean.TRUE);
        response.setCoordinates(coordinates);
        response.setWarning(warning);
        response.setDistanceKm(route.getPlannedDistanceKm());
        response.setDurationMin(route.getPlannedDurationMin());
        return response;
    }

    private List<RoutePathCoordinateResponse> collectWaypoints(RoutePlanEntity route, List<RouteStopEntity> stops) {
        List<RoutePathCoordinateResponse> waypoints = new ArrayList<>();
        appendCoordinate(waypoints, startLatitude(route), startLongitude(route));
        stops.stream()
                .sorted(Comparator.comparingInt(RouteStopEntity::getStopOrder))
                .forEach(stop -> appendCoordinate(waypoints, stop.getPickupPoint().getLatitude(),
                        stop.getPickupPoint().getLongitude()));
        appendCoordinate(waypoints, endLatitude(route), endLongitude(route));
        return waypoints;
    }

    private void appendCoordinate(List<RoutePathCoordinateResponse> target, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return;
        }
        target.add(new RoutePathCoordinateResponse(latitude, longitude));
    }

    private Double startLatitude(RoutePlanEntity route) {
        if (route.getStartSchool() != null) {
            return route.getStartSchool().getLatitude();
        }
        return route.getStartDepot() == null ? null : route.getStartDepot().getLatitude();
    }

    private Double startLongitude(RoutePlanEntity route) {
        if (route.getStartSchool() != null) {
            return route.getStartSchool().getLongitude();
        }
        return route.getStartDepot() == null ? null : route.getStartDepot().getLongitude();
    }

    private Double endLatitude(RoutePlanEntity route) {
        if (route.getEndSchool() != null) {
            return route.getEndSchool().getLatitude();
        }
        return route.getEndDepot() == null ? null : route.getEndDepot().getLatitude();
    }

    private Double endLongitude(RoutePlanEntity route) {
        if (route.getEndSchool() != null) {
            return route.getEndSchool().getLongitude();
        }
        return route.getEndDepot() == null ? null : route.getEndDepot().getLongitude();
    }
}
