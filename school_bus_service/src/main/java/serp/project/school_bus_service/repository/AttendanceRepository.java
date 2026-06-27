package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.AttendanceEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;


public interface AttendanceRepository extends BaseRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(Long tenantId);

    @Query(value = """
        SELECT a FROM AttendanceEntity a
        JOIN FETCH a.tripStudent ts
        JOIN FETCH ts.trip t
        JOIN FETCH t.route r
        JOIN FETCH ts.subscription sub
        JOIN FETCH sub.student student
        WHERE a.tenantId = :tenantId
          AND a.isDeleted = false
          AND (:tripId IS NULL OR t.id = :tripId)
          AND (:routeId IS NULL OR r.id = :routeId)
          AND (:dateFrom IS NULL OR a.recordedAt >= :dateFrom)
          AND (:dateTo IS NULL OR a.recordedAt < :dateTo)
    """,
    countQuery = """
        SELECT COUNT(a) FROM AttendanceEntity a
        JOIN a.tripStudent ts
        JOIN ts.trip t
        JOIN t.route r
        WHERE a.tenantId = :tenantId
          AND a.isDeleted = false
          AND (:tripId IS NULL OR t.id = :tripId)
          AND (:routeId IS NULL OR r.id = :routeId)
          AND (:dateFrom IS NULL OR a.recordedAt >= :dateFrom)
          AND (:dateTo IS NULL OR a.recordedAt < :dateTo)
    """)
    Page<AttendanceEntity> findReportAttendance(
            @Param("tenantId") Long tenantId,
            @Param("tripId") Long tripId,
            @Param("routeId") Long routeId,
            @Param("dateFrom") java.time.LocalDateTime dateFrom,
            @Param("dateTo") java.time.LocalDateTime dateTo,
            Pageable pageable);

    @Query("""
        SELECT a FROM AttendanceEntity a
        WHERE a.tripStudent.trip.route.id = :routeId
          AND a.tenantId = :tenantId
          AND a.isDeleted = false
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findByRouteIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(
            @Param("routeId") Long routeId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT a FROM AttendanceEntity a
        WHERE a.tripStudent.trip.id = :tripId
          AND a.tenantId = :tenantId
          AND a.isDeleted = false
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(
            @Param("tripId") Long tripId,
            @Param("tenantId") Long tenantId);

    @Query("""
        SELECT a FROM AttendanceEntity a
        WHERE a.tripStudent.trip.id = :tripId
          AND a.tripStudent.subscription.student.id = :studentId
          AND a.tenantId = :tenantId
          AND a.isDeleted = false
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(
            @Param("tripId") Long tripId,
            @Param("studentId") Long studentId,
            @Param("tenantId") Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query("""
        SELECT a FROM AttendanceEntity a
        WHERE a.tenantId = :tenantId AND a.isDeleted = false
          AND (:schoolId IS NULL OR a.tripStudent.trip.route.planningSession.school.id = :schoolId)
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findRecentAttendanceFiltered(
        @Param("tenantId") Long tenantId,
        @Param("schoolId") Long schoolId,
        Pageable pageable
    );

    @Query("""
        SELECT a FROM AttendanceEntity a
        WHERE a.tenantId = :tenantId AND a.isDeleted = false
          AND a.tripStudent.subscription.student.parentProfile.id = :parentProfileId
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findRecentAttendanceForParent(
        @Param("tenantId") Long tenantId,
        @Param("parentProfileId") Long parentProfileId,
        Pageable pageable
    );

    @Query("""
        SELECT a FROM AttendanceEntity a
        JOIN a.tripStudent.trip t
        WHERE a.tenantId = :tenantId AND a.isDeleted = false
          AND EXISTS (
              SELECT assignment FROM RouteAssignmentEntity assignment
              WHERE assignment.route.id = t.route.id
                AND assignment.driver.id = :driverId
                AND assignment.tenantId = :tenantId
                AND assignment.isDeleted = false
          )
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findRecentAttendanceForDriver(
        @Param("tenantId") Long tenantId,
        @Param("driverId") Long driverId,
        Pageable pageable
    );

    @Query("""
        SELECT a FROM AttendanceEntity a
        JOIN a.tripStudent.trip t
        WHERE a.tenantId = :tenantId AND a.isDeleted = false
          AND EXISTS (
              SELECT assignment FROM RouteAssignmentEntity assignment
              WHERE assignment.route.id = t.route.id
                AND assignment.attendant.id = :attendantId
                AND assignment.tenantId = :tenantId
                AND assignment.isDeleted = false
          )
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findRecentAttendanceForAttendant(
        @Param("tenantId") Long tenantId,
        @Param("attendantId") Long attendantId,
        Pageable pageable
    );
}

