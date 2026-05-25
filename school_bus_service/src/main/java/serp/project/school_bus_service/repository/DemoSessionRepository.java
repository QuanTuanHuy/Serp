package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.DemoSessionEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface DemoSessionRepository extends BaseRepository<DemoSessionEntity, Long> {

    Optional<DemoSessionEntity> findFirstByTripIdAndTenantIdAndIsDeletedFalseOrderByIdDesc(Long tripId,
            Long tenantId);

    List<DemoSessionEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByIdDesc(Long tripId, Long tenantId);
}

