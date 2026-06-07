package serp.project.school_bus_service.repository;

import org.springframework.stereotype.Repository;
import serp.project.school_bus_service.entity.RouteCalculationTraceEntity;
import serp.project.school_bus_service.enums.RouteCalculationType;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteCalculationTraceRepository extends BaseRepository<RouteCalculationTraceEntity, Long> {

    List<RouteCalculationTraceEntity> findByRoutePlanIdAndIsDeletedFalseOrderByCreatedAtDesc(Long routePlanId);

    Optional<RouteCalculationTraceEntity> findFirstByRoutePlanIdAndCalculationTypeAndIsDeletedFalseOrderByCreatedAtDesc(
            Long routePlanId,
            RouteCalculationType calculationType
    );
}
