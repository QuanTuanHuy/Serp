/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.IssueSecurityLevelMemberModel;

import java.util.List;

@Repository
public interface IIssueSecurityLevelMemberRepository extends JpaRepository<IssueSecurityLevelMemberModel, Long> {
    @Query("SELECT m FROM IssueSecurityLevelMemberModel m WHERE m.levelId = :levelId AND (m.tenantId = :tenantId OR m.tenantId = 0) ORDER BY m.id ASC")
    List<IssueSecurityLevelMemberModel> findAllByLevelIdAndTenantIdOrSystemTenant(@Param("levelId") Long levelId,
                                                                                   @Param("tenantId") Long tenantId);
}
