/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.IssueTypeScreenSchemeItemModel;

import java.util.List;

@Repository
public interface IIssueTypeScreenSchemeItemRepository extends JpaRepository<IssueTypeScreenSchemeItemModel, Long> {
    @Query("SELECT i FROM IssueTypeScreenSchemeItemModel i WHERE i.schemeId = :schemeId AND (i.tenantId = :tenantId OR i.tenantId = 0) ORDER BY i.id ASC")
    List<IssueTypeScreenSchemeItemModel> findAllBySchemeIdAndTenantIdOrSystemTenant(@Param("schemeId") Long schemeId,
                                                                                     @Param("tenantId") Long tenantId);
}
