package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.repository.projection.DriverFleetSummaryProjection;

import java.util.List;
import java.util.Optional;

public interface DriverProfileRepository extends BaseRepository<DriverProfileEntity, Long> {
    List<DriverProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    Optional<DriverProfileEntity> findByTenantIdAndUserIdAndIsDeletedFalse(Long tenantId, Long userId);

    @Query("""
            select count(d.id) as totalDrivers,
                   coalesce(sum(case
                       when d.isActive = true and upper(d.status) not in ('ON_LEAVE', 'INACTIVE') then 1
                       else 0
                   end), 0) as availableDrivers
            from DriverProfileEntity d
            where d.tenantId = :tenantId
              and d.isDeleted = false
            """)
    DriverFleetSummaryProjection getFleetSummary(@Param("tenantId") Long tenantId);
}
