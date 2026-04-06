package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.kernel.shared.base.BaseRepository;
import serp.project.school_bus_service.infrastructure.store.model.TransportRequestEntity;
import serp.project.school_bus_service.enums.RequestStatus;

import java.util.List;

public interface TransportRequestRepository extends BaseRepository<TransportRequestEntity, Long> {
    List<TransportRequestEntity> findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(Long tenantId);

    List<TransportRequestEntity> findBySchool_IdAndTenantIdAndStatusAndIsDeletedFalseOrderByCreatedAtAsc(Long schoolId, Long tenantId,
            RequestStatus status);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RequestStatus status);
}
