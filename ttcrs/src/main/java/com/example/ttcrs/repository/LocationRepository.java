package com.example.ttcrs.repository;

import com.example.ttcrs.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho {@link LocationEntity}.
 */
@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    /**
     * Lấy tất cả location thuộc một tenant.
     *
     * @param tenantId ID của tenant
     * @return danh sách LocationEntity
     */
    List<LocationEntity> findAllByTenantId(Long tenantId);
}
