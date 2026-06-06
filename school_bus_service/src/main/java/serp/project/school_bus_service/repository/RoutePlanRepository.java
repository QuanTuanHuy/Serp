package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.enums.RouteStatus;

import java.util.List;

public interface RoutePlanRepository extends BaseRepository<RoutePlanEntity, Long> {

    List<RoutePlanEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescIdDesc(Long tenantId);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RouteStatus status);

    @Query("SELECT r FROM RoutePlanEntity r WHERE r.planningSession.id = :sessionId AND r.tenantId = :tenantId AND r.isDeleted = false ORDER BY r.id ASC")
    List<RoutePlanEntity> findByPlanningSessionIdAndTenantId(@Param("sessionId") Long sessionId,
                                                              @Param("tenantId") Long tenantId);
}
