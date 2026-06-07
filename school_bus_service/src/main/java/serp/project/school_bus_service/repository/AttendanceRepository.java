package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.AttendanceEntity;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.util.List;


public interface AttendanceRepository extends BaseRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(Long tenantId);

    List<AttendanceEntity> findByRouteIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(Long routeId, Long tenantId);

    List<AttendanceEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(Long tripId, Long tenantId);

    List<AttendanceEntity> findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalseOrderByRecordedAtDesc(
            Long tripId,
            Long studentId,
            Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query("""
        SELECT a FROM AttendanceEntity a
        WHERE a.tenantId = :tenantId AND a.isDeleted = false
          AND (:schoolId IS NULL OR a.route.school.id = :schoolId)
        ORDER BY a.recordedAt DESC
    """)
    List<AttendanceEntity> findRecentAttendanceFiltered(
        @Param("tenantId") Long tenantId,
        @Param("schoolId") Long schoolId,
        Pageable pageable
    );
}

