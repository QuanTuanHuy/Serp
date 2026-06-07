package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.enums.RouteStatus;

import java.time.LocalDate;
import java.util.List;


public interface RoutePlanRepository extends BaseRepository<RoutePlanEntity, Long> {

    List<RoutePlanEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescIdDesc(Long tenantId);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RouteStatus status);

    @Query("SELECT r FROM RoutePlanEntity r WHERE r.planningSession.id = :sessionId AND r.tenantId = :tenantId AND r.isDeleted = false ORDER BY r.id ASC")
    List<RoutePlanEntity> findByPlanningSessionIdAndTenantId(@Param("sessionId") Long sessionId,
                                                              @Param("tenantId") Long tenantId);

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.serviceDate = :serviceDate
          AND r.status IN (serp.project.school_bus_service.enums.RouteStatus.PUBLISHED,
                           serp.project.school_bus_service.enums.RouteStatus.ASSIGNED,
                           serp.project.school_bus_service.enums.RouteStatus.TRIP_CREATED)
          AND (:schoolId IS NULL OR r.school.id = :schoolId)
          AND (:direction IS NULL OR r.routeDirection = :direction)
    """)
    List<RoutePlanEntity> findOperationalRoutes(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") serp.project.school_bus_service.enums.RouteDirection direction
    );

}
