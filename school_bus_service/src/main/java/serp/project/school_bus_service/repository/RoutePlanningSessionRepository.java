package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.RoutePlanningSessionEntity;
import serp.project.school_bus_service.enums.PlanningSessionStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoutePlanningSessionRepository extends BaseRepository<RoutePlanningSessionEntity, Long> {

    List<RoutePlanningSessionEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescIdDesc(Long tenantId);

    List<RoutePlanningSessionEntity> findByTenantIdAndStatusAndIsDeletedFalse(
            Long tenantId, PlanningSessionStatus status);

    @Query("""
            SELECT s FROM RoutePlanningSessionEntity s
            WHERE s.tenantId = :tenantId
              AND s.school.id = :schoolId
              AND s.schoolSchedule.id = :scheduleId
              AND s.serviceDate = :serviceDate
              AND s.routeDirection = :direction
              AND s.isDeleted = false
              AND s.status <> 'CANCELLED'
            ORDER BY s.id DESC
            """)
    List<RoutePlanningSessionEntity> findActiveByContext(
            @Param("tenantId") Long tenantId,
            @Param("schoolId") Long schoolId,
            @Param("scheduleId") Long scheduleId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("direction") RouteDirection direction);
}
