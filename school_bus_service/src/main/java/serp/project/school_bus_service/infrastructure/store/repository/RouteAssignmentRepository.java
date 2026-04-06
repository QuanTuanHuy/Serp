package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.RouteAssignmentEntity;

import java.util.List;
import java.util.Optional;

public interface RouteAssignmentRepository extends BaseRepository<RouteAssignmentEntity, Long> {
    Optional<RouteAssignmentEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    List<RouteAssignmentEntity> findByBusIdAndTenantIdAndIsDeletedFalse(Long busId, Long tenantId);

    List<RouteAssignmentEntity> findByDriverIdAndTenantIdAndIsDeletedFalse(Long driverId, Long tenantId);

    List<RouteAssignmentEntity> findByAttendantIdAndTenantIdAndIsDeletedFalse(Long attendantId, Long tenantId);
}
