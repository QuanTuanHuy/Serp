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
import serp.project.pmcore.infrastructure.store.model.IssueTypeSchemeItemModel;

import java.util.List;

@Repository
public interface IIssueTypeSchemeItemRepository extends JpaRepository<IssueTypeSchemeItemModel, Long> {

    List<IssueTypeSchemeItemModel> findAllByTenantIdAndSchemeIdOrderBySequenceAsc(Long tenantId, Long schemeId);

    @Query("SELECT i FROM IssueTypeSchemeItemModel i WHERE i.schemeId = :schemeId AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY i.sequence ASC")
    List<IssueTypeSchemeItemModel> findAllBySchemeIdAndTenantIdOrSystemTenant(@Param("schemeId") Long schemeId, @Param("tenantId") Long tenantId);

    boolean existsByTenantIdAndSchemeIdAndIssueTypeId(Long tenantId, Long schemeId, Long issueTypeId);

    @Modifying
    @Query("UPDATE IssueTypeSchemeItemModel i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.schemeId = :schemeId AND i.tenantId = :tenantId AND i.deletedAt IS NULL")
    void deleteBySchemeIdAndTenantId(@Param("schemeId") Long schemeId, @Param("tenantId") Long tenantId);
}
