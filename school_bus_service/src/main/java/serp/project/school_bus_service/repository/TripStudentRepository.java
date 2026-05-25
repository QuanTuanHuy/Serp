package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

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

