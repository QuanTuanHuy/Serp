package com.example.ttcrs.repository;

import com.example.ttcrs.entity.DriverStatusEntity;
import com.example.ttcrs.entity.DriverStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverStatusRepository extends JpaRepository<DriverStatusEntity, DriverStatusId> {
    Optional<DriverStatusEntity> findByUserIdAndTenantId(Long userId, Long tenantId);
    List<DriverStatusEntity> findAllByTenantId(Long tenantId);
}
