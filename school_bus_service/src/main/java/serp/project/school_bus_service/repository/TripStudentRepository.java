package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.enums.RouteDirection;
import serp.project.school_bus_service.shared.base.BaseRepository;


import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface TripStudentRepository extends BaseRepository<TripStudentEntity, Long> {

    @Query("""
        SELECT ts FROM TripStudentEntity ts
        WHERE ts.trip.id = :tripId
          AND ts.tenantId = :tenantId
          AND ts.isDeleted = false
        ORDER BY ts.subscription.student.fullName ASC
    """)
    List<TripStudentEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT ts FROM TripStudentEntity ts
        WHERE ts.trip.id IN :tripIds
          AND ts.tenantId = :tenantId
          AND ts.isDeleted = false
        ORDER BY ts.subscription.student.fullName ASC
    """)
    List<TripStudentEntity> findByTripIdInAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(
            @Param("tripIds") Collection<Long> tripIds,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT ts FROM TripStudentEntity ts
        WHERE ts.trip.id = :tripId
          AND ts.subscription.student.id = :studentId
          AND ts.tenantId = :tenantId
          AND ts.isDeleted = false
    """)
    Optional<TripStudentEntity> findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalse(
            @Param("tripId") Long tripId,
            @Param("studentId") Long studentId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT COUNT(ts) > 0 FROM TripStudentEntity ts
        WHERE ts.trip.id = :tripId
          AND ts.subscription.student.parentProfile.id = :parentProfileId
          AND ts.isDeleted = false
    """)
    boolean existsByTripIdAndStudentParentProfileIdAndIsDeletedFalse(
            @Param("tripId") Long tripId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.planningSession.school.id = :schoolId)
          AND (:direction IS NULL OR t.route.planningSession.routeDirection = :direction)
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusFiltered(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("schoolId") Long schoolId,
        @Param("direction") RouteDirection direction
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND ts.subscription.student.parentProfile.id = :parentProfileId
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusForParent(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("parentProfileId") Long parentProfileId
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.driver.id = :driverId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusForDriver(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("driverId") Long driverId
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.route.planningSession.serviceDate = :serviceDate
          AND EXISTS (
              SELECT a FROM RouteAssignmentEntity a
              WHERE a.route.id = t.route.id
                AND a.attendant.id = :attendantId
                AND a.tenantId = :tenantId
                AND a.isDeleted = false
          )
        GROUP BY ts.status
    """)
    List<Object[]> countAttendanceByStatusForAttendant(
        @Param("tenantId") Long tenantId,
        @Param("serviceDate") LocalDate serviceDate,
        @Param("attendantId") Long attendantId
    );

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.subscription.student.isDeleted = false
          AND ts.subscription.student.isActive = true
          AND t.isDeleted = false
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
              OR (:parentProfileId IS NOT NULL AND ts.subscription.student.parentProfile.id = :parentProfileId)
          )
        GROUP BY ts.status
    """)
    List<Object[]> countDashboardAttendanceByStatus(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT COUNT(DISTINCT ts.subscription.student.id) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.subscription.student.isDeleted = false
          AND ts.subscription.student.isActive = true
          AND t.isDeleted = false
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
              OR (:parentProfileId IS NOT NULL AND ts.subscription.student.parentProfile.id = :parentProfileId)
          )
    """)
    long countDashboardStudents(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);

    @Query("""
        SELECT COUNT(DISTINCT ts.subscription.student.parentProfile.id) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.subscription.student.isDeleted = false
          AND ts.subscription.student.isActive = true
          AND t.isDeleted = false
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
              OR (:parentProfileId IS NOT NULL AND ts.subscription.student.parentProfile.id = :parentProfileId)
          )
    """)
    long countDashboardParents(
            @Param("tenantId") Long tenantId,
            @Param("serviceDate") LocalDate serviceDate,
            @Param("schoolId") Long schoolId,
            @Param("direction") RouteDirection direction,
            @Param("tenantWide") boolean tenantWide,
            @Param("driverProfileId") Long driverProfileId,
            @Param("attendantProfileId") Long attendantProfileId,
            @Param("parentProfileId") Long parentProfileId);
}

