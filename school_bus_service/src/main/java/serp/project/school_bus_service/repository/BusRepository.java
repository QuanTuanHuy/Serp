package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.BusEntity;

import java.util.List;

public interface BusRepository extends BaseRepository<BusEntity, Long> {
    List<BusEntity> findByTenantIdAndIsDeletedFalseOrderByPlateNumberAsc(Long tenantId);

    List<BusEntity> findByTenantIdAndHomeDepotIdAndIsDeletedFalseOrderByPlateNumberAsc(Long tenantId, Long homeDepotId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);

    boolean existsByPlateNumberAndTenantIdAndIsDeletedFalseAndIdNot(
            String plateNumber, Long tenantId, Long excludeId);
}
