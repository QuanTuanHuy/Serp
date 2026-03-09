/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.PriorityModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPriorityRepository extends JpaRepository<PriorityModel, Long> {

    Optional<PriorityModel> findByIdAndTenantId(Long id, Long tenantId);

    List<PriorityModel> findAllByTenantIdOrderBySequenceAsc(Long tenantId);

    boolean existsByTenantIdAndName(Long tenantId, String name);

    @Modifying
    @Query("UPDATE PriorityModel p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
