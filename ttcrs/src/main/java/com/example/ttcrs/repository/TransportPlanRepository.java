package com.example.ttcrs.repository;

import com.example.ttcrs.entity.TransportPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportPlanRepository extends JpaRepository<TransportPlanEntity, Long> {
    List<TransportPlanEntity> findAllByTenantIdOrderByCreatedStampDesc(Long tenantId);
    Optional<TransportPlanEntity> findByIdAndTenantId(Long id, Long tenantId);
}
