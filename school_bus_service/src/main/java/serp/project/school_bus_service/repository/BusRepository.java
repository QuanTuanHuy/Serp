package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.repository.projection.BusFleetSummaryProjection;

import java.util.List;

public interface BusRepository extends BaseRepository<BusEntity, Long> {
    List<BusEntity> findByTenantIdAndIsDeletedFalseOrderByPlateNumberAsc(Long tenantId);

    List<BusEntity> findByTenantIdAndHomeDepotIdAndIsDeletedFalseOrderByPlateNumberAsc(Long tenantId, Long homeDepotId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    @Query("""
            select count(b.id) as totalBuses,
                   coalesce(sum(case
                       when b.isActive = true and upper(b.status) in ('AVAILABLE', 'ACTIVE') then 1
                       else 0
                   end), 0) as availableBuses
            from BusEntity b
            where b.tenantId = :tenantId
              and b.isDeleted = false
            """)
    BusFleetSummaryProjection getFleetSummary(@Param("tenantId") Long tenantId);

    boolean existsByPlateNumberAndTenantIdAndIsDeletedFalseAndIdNot(
            String plateNumber, Long tenantId, Long excludeId);
}
