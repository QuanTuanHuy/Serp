package com.example.ttcrs.repository;

import com.example.ttcrs.entity.TrailerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrailerRepository extends JpaRepository<TrailerEntity, Long> {
    List<TrailerEntity> findAllByTenantId(Long tenantId);
    boolean existsByCode(String code);
}
