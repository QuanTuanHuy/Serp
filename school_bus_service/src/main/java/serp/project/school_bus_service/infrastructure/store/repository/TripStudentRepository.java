package serp.project.school_bus_service.infrastructure.store.repository;

import serp.project.school_bus_service.infrastructure.store.model.TripStudentEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface TripStudentRepository extends BaseRepository<TripStudentEntity, Long> {

    List<TripStudentEntity> findByTripIdAndTenantIdAndIsDeletedFalseOrderByStudentFullNameAsc(Long tripId,
            Long tenantId);

    Optional<TripStudentEntity> findByTripIdAndStudentIdAndTenantIdAndIsDeletedFalse(
            Long tripId,
            Long studentId,
            Long tenantId);
}

