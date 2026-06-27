package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.dto.response.TripExecutionListItemResponse;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TripExecutionRepository extends BaseRepository<TripExecutionEntity, Long> {

    Optional<TripExecutionEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    @Query(value = """
        SELECT new serp.project.school_bus_service.dto.response.TripExecutionListItemResponse(
            t.id,
            t.tenantId,
            t.isActive,
            t.isDeleted,
            t.createdAt,
            t.createdBy,
            t.updatedAt,
            t.updatedBy,
            t.tripCode,
            r.id,
            r.routeCode,
            r.routeName,
            planningSession.serviceDate,
            planningSession.routeDirection,
            t.status,
            t.plannedStartAt,
            t.plannedEndAt,
            t.startedAt,
            t.completedAt,
            t.cancelledAt,
            t.cancellationReason
        )
        FROM TripExecutionEntity t
        JOIN t.route r
        JOIN r.planningSession planningSession
        WHERE t.tenantId = :tenantId
          AND t.isDeleted = false
          AND r.isDeleted = false
          AND planningSession.isDeleted = false
          AND (:routeId IS NULL OR r.id = :routeId)
          AND (:schoolId IS NULL OR planningSession.school.id = :schoolId)
          AND (:serviceDate IS NULL OR planningSession.serviceDate = :serviceDate)
          AND (:direction IS NULL OR planningSession.routeDirection = :direction)
          AND (:status IS NULL OR t.status = :status)
          AND (
              :keyword IS NULL
              OR LOWER(t.tripCode) LIKE :keyword
              OR LOWER(r.routeCode) LIKE :keyword
              OR LOWER(r.routeName) LIKE :keyword
          )
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = r.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
                    AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                     serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = r.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
                    AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                     serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT ts FROM TripStudentEntity ts
                  WHERE ts.trip.id = t.id
                    AND ts.subscription.student.parentProfile.id = :parentProfileId
                    AND ts.tenantId = :tenantId
                    AND ts.isDeleted = false
                    AND ts.subscription.isDeleted = false
                    AND ts.subscription.student.isDeleted = false
              ))
          )
    """, countQuery = """
        SELECT COUNT(t)
        FROM TripExecutionEntity t
        JOIN t.route r
        JOIN r.planningSession planningSession
        WHERE t.tenantId = :tenantId
          AND t.isDeleted = false
          AND r.isDeleted = false
          AND planningSession.isDeleted = false
          AND (:routeId IS NULL OR r.id = :routeId)
          AND (:schoolId IS NULL OR planningSession.school.id = :schoolId)
          AND (:serviceDate IS NULL OR planningSession.serviceDate = :serviceDate)
          AND (:direction IS NULL OR planningSession.routeDirection = :direction)
          AND (:status IS NULL OR t.status = :status)
          AND (
              :keyword IS NULL
              OR LOWER(t.tripCode) LIKE :keyword
              OR LOWER(r.routeCode) LIKE :keyword
              OR LOWER(r.routeName) LIKE :keyword
          )
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = r.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
                    AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                     serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = r.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
                    AND a.status IN (serp.project.school_bus_service.enums.RouteAssignmentStatus.ASSIGNED,
                                     serp.project.school_bus_service.enums.RouteAssignmentStatus.CONFIRMED)
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT ts FROM TripStudentEntity ts
                  WHERE ts.trip.id = t.id
                    AND ts.subscription.student.parentProfile.id = :parentProfileId
                    AND ts.tenantId = :tenantId
                    AND ts.isDeleted = false
                    AND ts.subscription.isDeleted = false
                    AND ts.subscription.student.isDeleted = false
              ))
          )
    """)
    Page<TripExecutionListItemResponse> findTripListItems(
            @Param("tenantId") Long tenantId,
            @Param("routeId") Long routeId,
            @Param("schoolId") Long schoolId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("direction") RouteDirection direction,
            @Param("status") TripStatus status,
            @Param("keyword") String keyword,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId,
            Pageable pageable);

    @Query("""
        SELECT t FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId
          AND t.isDeleted = false
          AND (:dateFrom IS NULL OR t.route.planningSession.serviceDate >= :dateFrom)
          AND (:dateTo IS NULL OR t.route.planningSession.serviceDate <= :dateTo)
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:routeId IS NULL OR t.route.id = :routeId)
          AND (:tripId IS NULL OR t.id = :tripId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (:status IS NULL OR t.status = :status)
    """)
    Page<TripExecutionEntity> findReportTrips(
            @Param("tenantId") Long tenantId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("schoolId") Long schoolId,
            @Param("routeId") Long routeId,
            @Param("tripId") Long tripId,
            @Param("direction") RouteDirection direction,
            @Param("status") TripStatus status,
            Pageable pageable);

    @Query("""
        SELECT t FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId
          AND t.route.planningSession.serviceDate = :serviceDate
          AND t.isDeleted = false
        ORDER BY t.id DESC
    """)
    List<TripExecutionEntity> findByTenantIdAndServiceDateAndIsDeletedFalseOrderByIdDesc(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, TripStatus status);

    @Query("SELECT MAX(t.route.planningSession.serviceDate) FROM TripExecutionEntity t WHERE t.tenantId = :tenantId AND t.isDeleted = false")
    Optional<LocalDate> findLatestServiceDate(@Param("tenantId") Long tenantId);

    @Query("""
        SELECT t.status, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
        GROUP BY t.status
    """)
    List<Object[]> countTripsByStatusFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT t.route.planningSession.serviceDate, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate >= :fromDate AND t.route.planningSession.serviceDate <= :toDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
        GROUP BY t.route.planningSession.serviceDate
        ORDER BY t.route.planningSession.serviceDate ASC
    """)
    List<Object[]> countTripsByDateFiltered(
        @Param("tenantId") Long tenantId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT t.route.planningSession.routeDirection, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
        GROUP BY t.route.planningSession.routeDirection
    """)
    List<Object[]> countTripsByDirectionFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT t FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
    """)
    List<TripExecutionEntity> findTripsFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT t FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT ts FROM TripStudentEntity ts
              WHERE ts.trip.id = t.id
                AND ts.subscription.student.parentProfile.id = :parentProfileId
                AND ts.isDeleted = false
          )
    """)
    List<TripExecutionEntity> findTripsByParentAndDate(
        @Param("tenantId") Long tenantId,
        @Param("parentProfileId") Long parentProfileId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT t.status, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT ts FROM TripStudentEntity ts
              WHERE ts.trip.id = t.id
                AND ts.subscription.student.parentProfile.id = :parentProfileId
                AND ts.isDeleted = false
          )
        GROUP BY t.status
    """)
    List<Object[]> countTripsByStatusForParent(
        @Param("tenantId") Long tenantId,
        @Param("parentProfileId") Long parentProfileId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT t FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.driver.id = :driverId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
    """)
    List<TripExecutionEntity> findTripsByDriverAndDate(
        @Param("tenantId") Long tenantId,
        @Param("driverId") Long driverId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT t.status, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.driver.id = :driverId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
        GROUP BY t.status
    """)
    List<Object[]> countTripsByStatusForDriver(
        @Param("tenantId") Long tenantId,
        @Param("driverId") Long driverId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT t FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.attendant.id = :attendantId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
    """)
    List<TripExecutionEntity> findTripsByAttendantAndDate(
        @Param("tenantId") Long tenantId,
        @Param("attendantId") Long attendantId,
        @Param("serviceDate") LocalDate serviceDate
    );

    @Query("""
        SELECT t.status, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.attendant.id = :attendantId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
        GROUP BY t.status
    """)
    List<Object[]> countTripsByStatusForAttendant(
        @Param("tenantId") Long tenantId,
        @Param("attendantId") Long attendantId,
        @Param("serviceDate") LocalDate serviceDate
    );

    List<TripExecutionEntity> findByRouteIdInAndTenantIdAndIsDeletedFalse(
            Collection<Long> routeIds,
            Long tenantId);

    @Query("""
        SELECT MAX(t.route.planningSession.serviceDate) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT ts FROM TripStudentEntity ts
                  WHERE ts.trip.id = t.id
                    AND ts.subscription.student.parentProfile.id = :parentProfileId
                    AND ts.tenantId = :tenantId
                    AND ts.isDeleted = false
                    AND ts.subscription.student.isDeleted = false
                    AND ts.subscription.student.isActive = true
              ))
          )
    """)
    Optional<LocalDate> findLatestDashboardServiceDate(
            @Param("tenantId") Long tenantId,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT t.status, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT ts FROM TripStudentEntity ts
                  WHERE ts.trip.id = t.id
                    AND ts.subscription.student.parentProfile.id = :parentProfileId
                    AND ts.tenantId = :tenantId
                    AND ts.isDeleted = false
                    AND ts.subscription.student.isDeleted = false
                    AND ts.subscription.student.isActive = true
              ))
          )
        GROUP BY t.status
    """)
    List<Object[]> countDashboardTripsByStatus(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT t.route.planningSession.serviceDate, COUNT(t) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.route.planningSession.serviceDate BETWEEN :fromDate AND :toDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT ts FROM TripStudentEntity ts
                  WHERE ts.trip.id = t.id
                    AND ts.subscription.student.parentProfile.id = :parentProfileId
                    AND ts.tenantId = :tenantId
                    AND ts.isDeleted = false
                    AND ts.subscription.student.isDeleted = false
                    AND ts.subscription.student.isActive = true
              ))
          )
        GROUP BY t.route.planningSession.serviceDate
        ORDER BY t.route.planningSession.serviceDate ASC
    """)
    List<Object[]> countDashboardTripsByDate(
            @Param("tenantId") Long tenantId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT COUNT(DISTINCT t.route.planningSession.school.id) FROM TripExecutionEntity t
        WHERE t.tenantId = :tenantId AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.driver.id = :driverProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:attendantProfileId IS NOT NULL AND EXISTS (
                  SELECT a FROM RouteAssignmentEntity a
                  WHERE a.route.id = t.route.id
                    AND a.attendant.id = :attendantProfileId
                    AND a.tenantId = :tenantId
                    AND a.isDeleted = false
              ))
              OR (:parentProfileId IS NOT NULL AND EXISTS (
                  SELECT ts FROM TripStudentEntity ts
                  WHERE ts.trip.id = t.id
                    AND ts.subscription.student.parentProfile.id = :parentProfileId
                    AND ts.tenantId = :tenantId
                    AND ts.isDeleted = false
                    AND ts.subscription.student.isDeleted = false
                    AND ts.subscription.student.isActive = true
              ))
          )
    """)
    long countDashboardSchools(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);
}

