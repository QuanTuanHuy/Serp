package serp.project.school_bus_service.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.repository.projection.RouteStopCountProjection;
import serp.project.school_bus_service.repository.projection.RouteTerminalStopProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.RouteStopEntity;

import java.util.Collection;
import java.util.List;

public interface RouteStopRepository extends BaseRepository<RouteStopEntity, Long> {
    List<RouteStopEntity> findByRouteIdAndTenantIdAndIsDeletedFalseOrderByStopOrderAsc(Long routeId, Long tenantId);

    @Query(value = """
            SELECT rs.route_id AS routeId,
                   CAST(COUNT(*) AS INTEGER) AS stopsCount
              FROM public.school_bus_route_stop rs
             WHERE rs.tenant_id = :tenantId
               AND rs.is_deleted = false
               AND rs.route_id IN (:routeIds)
             GROUP BY rs.route_id
            """, nativeQuery = true)
    List<RouteStopCountProjection> countActiveStopsByRouteIds(
            @Param("tenantId") Long tenantId,
            @Param("routeIds") Collection<Long> routeIds);

    @Query(value = """
            SELECT rs.route_id AS routeId,
                   rs.stop_purpose AS stopPurpose,
                   COALESCE(s.name, d.name, pp.name) AS locationName
              FROM public.school_bus_route_stop rs
              LEFT JOIN public.school_bus_school s
                     ON rs.location_type = 'SCHOOL'
                    AND s.id = rs.location_id
              LEFT JOIN public.school_bus_depot d
                     ON rs.location_type = 'DEPOT'
                    AND d.id = rs.location_id
              LEFT JOIN public.school_bus_pickup_point pp
                     ON rs.location_type = 'PICKUP_POINT'
                    AND pp.id = rs.location_id
             WHERE rs.tenant_id = :tenantId
               AND rs.is_deleted = false
               AND rs.route_id IN (:routeIds)
               AND rs.stop_purpose IN ('START_TERMINAL', 'END_TERMINAL')
            """, nativeQuery = true)
    List<RouteTerminalStopProjection> findTerminalStopsByRouteIds(
            @Param("tenantId") Long tenantId,
            @Param("routeIds") Collection<Long> routeIds);

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
