package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.AttendanceEntity;

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
}
