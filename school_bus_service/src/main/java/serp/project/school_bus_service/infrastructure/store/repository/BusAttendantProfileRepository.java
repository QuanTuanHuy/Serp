package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.BusAttendantProfileEntity;

import java.util.List;

public interface BusAttendantProfileRepository extends BaseRepository<BusAttendantProfileEntity, Long> {
    List<BusAttendantProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);
}
