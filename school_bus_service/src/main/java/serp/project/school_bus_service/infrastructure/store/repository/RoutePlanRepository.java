package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.RoutePlanEntity;
import serp.project.school_bus_service.enums.RouteStatus;

import java.util.List;

public interface RoutePlanRepository extends BaseRepository<RoutePlanEntity, Long> {
    List<RoutePlanEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescIdDesc(Long tenantId);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RouteStatus status);
}
