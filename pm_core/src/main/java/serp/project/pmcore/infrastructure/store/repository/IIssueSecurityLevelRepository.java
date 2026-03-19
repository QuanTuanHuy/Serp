/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.IssueSecurityLevelModel;

import java.util.List;

@Repository
public interface IIssueSecurityLevelRepository extends JpaRepository<IssueSecurityLevelModel, Long> {
    @Query("SELECT l FROM IssueSecurityLevelModel l WHERE l.schemeId = :schemeId AND (l.tenantId = :tenantId OR l.tenantId = 0) ORDER BY l.id ASC")
    List<IssueSecurityLevelModel> findAllBySchemeIdAndTenantIdOrSystemTenant(@Param("schemeId") Long schemeId,
                                                                              @Param("tenantId") Long tenantId);
}
