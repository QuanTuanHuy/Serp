package serp.project.school_bus_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.dto.response.RoutePlanListItemResponse;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.enums.RouteStatus;

import java.time.LocalDate;
import java.util.List;


public interface RoutePlanRepository extends BaseRepository<RoutePlanEntity, Long> {

    @Query(value = """
        SELECT new serp.project.school_bus_service.dto.response.RoutePlanListItemResponse(
            r.id,
            r.tenantId,
            r.isActive,
            r.isDeleted,
            r.createdAt,
            r.createdBy,
            r.updatedAt,
            r.updatedBy,
            ps.school.id,
            ps.school.name,
            ps.routeDirection,
            r.routeCode,
            r.routeName,
            ps.serviceDate,
            r.status,
            r.plannedDistanceKm,
            r.plannedDurationMin,
            r.plannedStudentCount,
            r.startedAt,
            r.completedAt,
            r.requiredCapacity,
            ps.id
        )
        FROM RoutePlanEntity r
        JOIN r.planningSession ps
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND (
              :keywordPattern IS NULL
              OR LOWER(r.routeCode) LIKE :keywordPattern
              OR LOWER(r.routeName) LIKE :keywordPattern
              OR LOWER(ps.school.name) LIKE :keywordPattern
              OR LOWER(STR(r.status)) LIKE :keywordPattern
              OR LOWER(STR(ps.routeDirection)) LIKE :keywordPattern
          )
        """,
        countQuery = """
        SELECT COUNT(r)
        FROM RoutePlanEntity r
        JOIN r.planningSession ps
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND (
              :keywordPattern IS NULL
              OR LOWER(r.routeCode) LIKE :keywordPattern
              OR LOWER(r.routeName) LIKE :keywordPattern
              OR LOWER(ps.school.name) LIKE :keywordPattern
              OR LOWER(STR(r.status)) LIKE :keywordPattern
              OR LOWER(STR(ps.routeDirection)) LIKE :keywordPattern
          )
        """)
    Page<RoutePlanListItemResponse> findRouteListItems(
            @Param("tenantId") Long tenantId,
            @Param("keywordPattern") String keywordPattern,
            Pageable pageable);

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
        ORDER BY r.planningSession.serviceDate DESC, r.id DESC
    """)
    List<RoutePlanEntity> findByTenantIdAndIsDeletedFalseOrderByServiceDateDescIdDesc(@Param("tenantId") Long tenantId);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RouteStatus status);

    @Query("SELECT r FROM RoutePlanEntity r WHERE r.planningSession.id = :sessionId AND r.tenantId = :tenantId AND r.isDeleted = false ORDER BY r.id ASC")
    List<RoutePlanEntity> findByPlanningSessionIdAndTenantId(@Param("sessionId") Long sessionId,
                                                              @Param("tenantId") Long tenantId);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND r.isActive = true
          AND r.planningSession.id = :sessionId
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = r.id
                AND a.bus.id = :busId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
          AND (:excludeRouteId IS NULL OR r.id <> :excludeRouteId)
    """)
    boolean existsActiveRouteUsingSelectedBusInSession(
        @Param("tenantId") Long tenantId,
        @Param("sessionId") Long sessionId,
        @Param("busId") Long busId,
        @Param("excludeRouteId") Long excludeRouteId
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND r.status IN (serp.project.school_bus_service.enums.RouteStatus.PUBLISHED,
                           serp.project.school_bus_service.enums.RouteStatus.ASSIGNED,
                           serp.project.school_bus_service.enums.RouteStatus.TRIP_CREATED)
          AND (:schoolId IS NULL OR r.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR r.planningSession.routeDirection = :direction)
    """)
    List<RoutePlanEntity> findOperationalRoutes(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT rps FROM RoutePlanStudentEntity rps
              WHERE rps.route.id = r.id
                AND rps.subscription.student.parentProfile.id = :parentProfileId
                AND rps.isDeleted = false
          )
    """)
    List<RoutePlanEntity> findRoutePlansByParentAndDate(
        @Param("tenantId") Long tenantId,
        @Param("parentProfileId") Long parentProfileId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = r.id
                AND a.driver.id = :driverId
                AND a.isDeleted = false
          )
    """)
    List<RoutePlanEntity> findRoutePlansByDriverAndDate(
        @Param("tenantId") Long tenantId,
        @Param("driverId") Long driverId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = r.id
                AND a.attendant.id = :attendantId
                AND a.isDeleted = false
          )
    """)
    List<RoutePlanEntity> findRoutePlansByAttendantAndDate(
        @Param("tenantId") Long tenantId,
        @Param("attendantId") Long attendantId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT r FROM RoutePlanEntity r
        WHERE r.tenantId = :tenantId
          AND r.isDeleted = false
          AND r.planningSession.serviceDate = :serviceDate
          AND r.status IN (serp.project.school_bus_service.enums.RouteStatus.PUBLISHED,
                           serp.project.school_bus_service.enums.RouteStatus.ASSIGNED,
                           serp.project.school_bus_service.enums.RouteStatus.TRIP_CREATED)
          AND (:schoolId IS NULL OR r.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR r.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND (
                  EXISTS (
                      SELECT a FROM RouteAssignmentEntity a
                      WHERE a.route.id = r.id
                        AND a.driver.id = :driverProfileId
                        AND a.tenantId = :tenantId
                        AND a.isDeleted = false
                        AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                         serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                  )
                  OR EXISTS (
                      SELECT t FROM TripExecutionEntity t
                      WHERE t.route.id = r.id
                        AND EXISTS (
                            SELECT a2 FROM RouteAssignmentEntity a2
                            WHERE a2.route.id = t.route.id
                              AND a2.driver.id = :driverProfileId
                              AND a2.tenantId = :tenantId
                              AND a2.isDeleted = false
                        )
                        AND t.tenantId = :tenantId
                        AND t.isDeleted = false
                  )
              ))
              OR (:attendantProfileId IS NOT NULL AND (
                  EXISTS (
                      SELECT a FROM RouteAssignmentEntity a
                      WHERE a.route.id = r.id
                        AND a.attendant.id = :attendantProfileId
                        AND a.tenantId = :tenantId
                        AND a.isDeleted = false
                        AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                         serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
                  )
                  OR EXISTS (
                      SELECT t FROM TripExecutionEntity t
                      WHERE t.route.id = r.id
                        AND EXISTS (
                            SELECT a3 FROM RouteAssignmentEntity a3
                            WHERE a3.route.id = t.route.id
                              AND a3.attendant.id = :attendantProfileId
                              AND a3.tenantId = :tenantId
                              AND a3.isDeleted = false
                        )
                        AND t.tenantId = :tenantId
                        AND t.isDeleted = false
                  )
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT rps FROM RoutePlanStudentEntity rps
                  WHERE rps.route.id = r.id
                    AND rps.subscription.student.parentProfile.id = :parentProfileId
                    AND rps.tenantId = :tenantId
                    AND rps.isDeleted = false
                    AND rps.subscription.student.isDeleted = false
                    AND rps.subscription.student.isActive = true
              ))
          )
    """)
    List<RoutePlanEntity> findDashboardRoutes(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);
}
