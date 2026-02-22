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

    boolean existsByTenantIdAndSchemeIdAndIssueTypeId(Long tenantId, Long schemeId, Long issueTypeId);

    @Modifying
    @Query("UPDATE IssueTypeSchemeItemModel i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.schemeId = :schemeId AND i.tenantId = :tenantId AND i.deletedAt IS NULL")
    void deleteBySchemeIdAndTenantId(@Param("schemeId") Long schemeId, @Param("tenantId") Long tenantId);
}
