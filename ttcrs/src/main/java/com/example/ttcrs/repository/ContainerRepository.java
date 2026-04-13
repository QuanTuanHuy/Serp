package com.example.ttcrs.repository;

import com.example.ttcrs.entity.ContainerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContainerRepository extends JpaRepository<ContainerEntity, Long> {
    List<ContainerEntity> findAllByTenantId(Long tenantId);
    boolean existsByCode(String code);
}
