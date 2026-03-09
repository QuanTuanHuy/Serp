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
import serp.project.pmcore.infrastructure.store.model.PrioritySchemeItemModel;

import java.util.List;

@Repository
public interface IPrioritySchemeItemRepository extends JpaRepository<PrioritySchemeItemModel, Long> {

    List<PrioritySchemeItemModel> findAllByTenantIdAndSchemeIdOrderBySequenceAsc(Long tenantId, Long schemeId);

    @Query("SELECT i FROM PrioritySchemeItemModel i WHERE i.schemeId = :schemeId AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY i.sequence ASC")
    List<PrioritySchemeItemModel> findAllBySchemeIdAndTenantIdOrSystemTenant(@Param("schemeId") Long schemeId, @Param("tenantId") Long tenantId);

    boolean existsByTenantIdAndSchemeIdAndPriorityId(Long tenantId, Long schemeId, Long priorityId);

    @Modifying
    @Query("UPDATE PrioritySchemeItemModel p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.schemeId = :schemeId AND p.tenantId = :tenantId AND p.deletedAt IS NULL")
    void deleteBySchemeIdAndTenantId(@Param("schemeId") Long schemeId, @Param("tenantId") Long tenantId);
}
