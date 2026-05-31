package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.shared.base.BaseRepository;
import serp.project.school_bus_service.entity.TransportRequestEntity;
import serp.project.school_bus_service.enums.RequestStatus;

import java.util.List;

public interface TransportRequestRepository extends BaseRepository<TransportRequestEntity, Long> {
    List<TransportRequestEntity> findByTenantIdAndIsDeletedFalseOrderByCreatedAtDesc(Long tenantId);

    List<TransportRequestEntity> findBySchool_IdAndTenantIdAndStatusAndIsDeletedFalseOrderByCreatedAtAsc(Long schoolId, Long tenantId,
            RequestStatus status);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, RequestStatus status);

    long countBySchoolIdAndTenantIdAndIsDeletedFalse(Long schoolId, Long tenantId);

    long countByTenantIdAndIsDeletedFalse(Long tenantId);
}
