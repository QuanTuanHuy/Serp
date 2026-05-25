package com.example.ttcrs.repository;

import com.example.ttcrs.entity.TransportPlanStopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public interface TransportPlanStopRepository extends JpaRepository<TransportPlanStopEntity, Long> {
    List<TransportPlanStopEntity> findAllByTransportPlanIdOrderBySequenceAsc(Long transportPlanId);
    List<TransportPlanStopEntity> findAllByTransportPlanIdIn(java.util.Collection<Long> planIds);

    default Map<Long, Long> countByPlanIds(java.util.Collection<Long> planIds) {
        return findAllByTransportPlanIdIn(planIds).stream()
                .collect(Collectors.groupingBy(
                        TransportPlanStopEntity::getTransportPlanId,
                        Collectors.counting()));
    }
}
