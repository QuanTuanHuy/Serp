package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.TransportRequestHistoryEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

public interface TransportRequestHistoryRepository extends BaseRepository<TransportRequestHistoryEntity, Long> {

    List<TransportRequestHistoryEntity> findByRequestIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(
            Long requestId,
            Long tenantId);
}

