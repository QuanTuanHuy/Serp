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

    List<TripStudentEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(Long tripId,
            Long tenantId);

    List<TripStudentEntity> findByTripIdInAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(Collection<Long> tripIds,
            Long tenantId);

    Optional<TripStudentEntity> findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalse(
            Long tripId,
            Long studentId,
            Long tenantId);

    boolean existsByTripIdAndStudentParentProfileIdAndIsDeletedFalse(Long tripId, Long parentProfileId);

    @Query("""
        SELECT ts.status, COUNT(ts) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId AND ts.isDeleted = false AND t.isDeleted = false
          AND t.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
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
          AND t.serviceDate = :serviceDate
          AND ts.student.parentProfile.id = :parentProfileId
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
          AND t.serviceDate = :serviceDate
          AND t.driver.id = :driverId
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
          AND t.serviceDate = :serviceDate
          AND t.attendant.id = :attendantId
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
          AND ts.student.isDeleted = false
          AND ts.student.isActive = true
          AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND t.driver.id = :driverProfileId)
              OR (:attendantProfileId IS NOT NULL AND t.attendant.id = :attendantProfileId)
              OR (:parentProfileId IS NOT NULL AND ts.student.parentProfile.id = :parentProfileId)
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
        SELECT COUNT(DISTINCT ts.student.id) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.student.isDeleted = false
          AND ts.student.isActive = true
          AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND t.driver.id = :driverProfileId)
              OR (:attendantProfileId IS NOT NULL AND t.attendant.id = :attendantProfileId)
              OR (:parentProfileId IS NOT NULL AND ts.student.parentProfile.id = :parentProfileId)
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
        SELECT COUNT(DISTINCT ts.student.parentProfile.id) FROM TripStudentEntity ts
        JOIN ts.trip t
        WHERE ts.tenantId = :tenantId
          AND ts.isDeleted = false
          AND ts.student.isDeleted = false
          AND ts.student.isActive = true
          AND t.isDeleted = false
          AND t.route.isDeleted = false
          AND t.serviceDate = :serviceDate
          AND (:schoolId IS NULL OR t.route.school.id = :schoolId)
          AND (:direction IS NULL OR t.routeDirection = :direction)
          AND (
              :tenantWide = true
              OR (:driverProfileId IS NOT NULL AND t.driver.id = :driverProfileId)
              OR (:attendantProfileId IS NOT NULL AND t.attendant.id = :attendantProfileId)
              OR (:parentProfileId IS NOT NULL AND ts.student.parentProfile.id = :parentProfileId)
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

