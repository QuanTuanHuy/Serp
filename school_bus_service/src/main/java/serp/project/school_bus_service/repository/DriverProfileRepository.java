package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.DriverProfileEntity;

import java.util.List;

public interface DriverProfileRepository extends BaseRepository<DriverProfileEntity, Long> {
    List<DriverProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);

    boolean existsByLicenseNumberAndTenantIdAndIsDeletedFalseAndIdNot(
            String licenseNumber, Long tenantId, Long excludeId);
}
