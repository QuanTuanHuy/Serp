package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.StudentSubscriptionHistoryEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

public interface StudentSubscriptionHistoryRepository
        extends BaseRepository<StudentSubscriptionHistoryEntity, Long> {

    List<StudentSubscriptionHistoryEntity> findBySubscriptionIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(
            Long subscriptionId, Long tenantId);

    List<StudentSubscriptionHistoryEntity> findBySourceRequestIdAndTenantIdAndIsDeletedFalseOrderByChangedAtDesc(
            Long sourceRequestId, Long tenantId);
}
