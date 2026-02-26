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
import serp.project.pmcore.infrastructure.store.model.WorkflowSchemeItemModel;

import java.util.List;

@Repository
public interface IWorkflowSchemeItemRepository extends JpaRepository<WorkflowSchemeItemModel, Long> {

    List<WorkflowSchemeItemModel> findAllByTenantIdAndSchemeId(Long tenantId, Long schemeId);

    @Query("SELECT i FROM WorkflowSchemeItemModel i WHERE i.schemeId = :schemeId AND (i.tenantId = :tenantId OR i.tenantId = 0)")
    List<WorkflowSchemeItemModel> findAllBySchemeIdAndTenantIdOrSystemTenant(@Param("schemeId") Long schemeId, @Param("tenantId") Long tenantId);

    @Modifying
    @Query("UPDATE WorkflowSchemeItemModel i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.schemeId = :schemeId AND i.tenantId = :tenantId AND i.deletedAt IS NULL")
    void deleteBySchemeIdAndTenantId(@Param("schemeId") Long schemeId, @Param("tenantId") Long tenantId);
}
