package serp.project.school_bus_service.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.RouteStopEntity;

import java.util.List;

public interface RouteStopRepository extends BaseRepository<RouteStopEntity, Long> {
    List<RouteStopEntity> findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(Long routeId, Long tenantId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update RouteStopEntity entity
               set entity.isDeleted = true,
                   entity.isActive = false,
                   entity.updatedBy = :updatedBy
             where entity.route.id = :routeId
               and entity.tenantId = :tenantId
               and entity.isDeleted = false
            """)
    int softDeleteByRouteIdAndTenantId(@Param("routeId") Long routeId, @Param("tenantId") Long tenantId,
            @Param("updatedBy") String updatedBy);
}
