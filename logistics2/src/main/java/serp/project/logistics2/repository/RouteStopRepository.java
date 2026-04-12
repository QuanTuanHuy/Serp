package serp.project.logistics2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import serp.project.logistics2.entity.RouteStopEntity;

public interface RouteStopRepository extends JpaRepository<RouteStopEntity, String> {

    Optional<RouteStopEntity> findByRouteIdAndSequence(String routeId, int stopSequence);

    List<RouteStopEntity> findByRouteIdOrderBySequenceAsc(String routeId);

}
