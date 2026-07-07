package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.repository.projection.AttendantFleetSummaryProjection;

import java.util.List;
import java.util.Optional;

public interface BusAttendantProfileRepository extends BaseRepository<BusAttendantProfileEntity, Long> {
    List<BusAttendantProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    Optional<BusAttendantProfileEntity> findByTenantIdAndUserIdAndIsDeletedFalse(Long tenantId, Long userId);

    @Query("""
            select count(a.id) as totalAttendants,
                   coalesce(sum(case
                       when a.isActive = true and upper(a.status) not in ('ON_LEAVE', 'INACTIVE') then 1
                       else 0
                   end), 0) as availableAttendants
            from BusAttendantProfileEntity a
            where a.tenantId = :tenantId
              and a.isDeleted = false
            """)
    AttendantFleetSummaryProjection getFleetSummary(@Param("tenantId") Long tenantId);
}
