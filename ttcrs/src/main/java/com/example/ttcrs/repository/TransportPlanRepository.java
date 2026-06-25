package com.example.ttcrs.repository;

import com.example.ttcrs.constant.TransportPlanStatus;
import com.example.ttcrs.entity.TransportPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportPlanRepository extends JpaRepository<TransportPlanEntity, Long> {
    List<TransportPlanEntity> findAllByTenantIdOrderByCreatedStampDesc(Long tenantId);
    List<TransportPlanEntity> findAllByTenantIdAndDriverIdOrderByStartTimeDesc(Long tenantId, Long driverId);
    Optional<TransportPlanEntity> findByIdAndTenantId(Long id, Long tenantId);
    Optional<TransportPlanEntity> findByIdAndTenantIdAndDriverId(Long id, Long tenantId, Long driverId);
    boolean existsByDriverIdAndStatus(Long driverId, TransportPlanStatus status);
}
