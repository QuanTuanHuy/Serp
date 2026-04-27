package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.infrastructure.store.model.TransportRequestHistoryEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;

import java.util.List;

public interface TransportRequestHistoryRepository extends BaseRepository<TransportRequestHistoryEntity, Long> {

    List<TransportRequestHistoryEntity> findByRequestIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(
            Long requestId,
            Long tenantId);
}

