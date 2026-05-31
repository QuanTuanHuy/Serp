package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.enums.TripStatus;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripExecutionRepository extends BaseRepository<TripExecutionEntity, Long> {

    Optional<TripExecutionEntity> findByRouteIdAndTenantIdAndIsDeletedFalse(Long routeId, Long tenantId);

    List<TripExecutionEntity> findByTenantIdAndServiceDateAndIsDeletedFalseOrderByIdDesc(Long tenantId,
            LocalDate serviceDate);

    long countByTenantIdAndStatusAndIsDeletedFalse(Long tenantId, TripStatus status);
}

