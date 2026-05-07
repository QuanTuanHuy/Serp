package com.example.ttcrs.repository;

import com.example.ttcrs.entity.TruckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TruckRepository extends JpaRepository<TruckEntity, Long> {
    List<TruckEntity> findAllByTenantId(Long tenantId);
    boolean existsByCode(String code);
}
