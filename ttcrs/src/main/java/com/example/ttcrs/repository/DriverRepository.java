package com.example.ttcrs.repository;

import com.example.ttcrs.entity.DriverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<DriverEntity, Long> {
    List<DriverEntity> findAllByTenantId(Long tenantId);
}
