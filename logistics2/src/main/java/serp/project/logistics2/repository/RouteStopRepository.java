package serp.project.logistics2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.logistics2.entity.RouteStopEntity;

public interface RouteStopRepository extends JpaRepository<RouteStopEntity, String> {

    Optional<RouteStopEntity> findByRouteIdAndSequence(String routeId, int stopSequence);

    List<RouteStopEntity> findByRouteIdOrderBySequenceAsc(String routeId);

    @Query("SELECT rs FROM RouteStopEntity rs, RouteEntity r " +
            "WHERE rs.routeId = r.id " +
            "AND r.vehicleShipperId = :vehicleShipperId " +
            "AND r.status = 'IN_PROGRESS' " +
            "AND rs.status = 'WAITING' " +
            "ORDER BY rs.sequence ASC")
    List<RouteStopEntity> findNextRouteStop(
            @Param("vehicleShipperId") String vehicleShipperId,
            Pageable pageable);
}
