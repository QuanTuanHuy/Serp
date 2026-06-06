package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;

import java.util.List;

public interface BusAttendantProfileRepository extends BaseRepository<BusAttendantProfileEntity, Long> {
    List<BusAttendantProfileEntity> findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(Long tenantId);
}
