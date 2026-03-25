/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkflowVersionModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkflowVersionRepository extends JpaRepository<WorkflowVersionModel, Long> {

    Optional<WorkflowVersionModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT v FROM WorkflowVersionModel v WHERE v.id = :id AND (v.tenantId = :tenantId OR v.tenantId = 0)")
    Optional<WorkflowVersionModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Query("SELECT v FROM WorkflowVersionModel v WHERE v.workflowId = :workflowId AND (v.tenantId = :tenantId OR v.tenantId = 0) ORDER BY v.versionNo ASC, v.id ASC")
    List<WorkflowVersionModel> findByWorkflowIdAndTenantIdOrSystemTenant(@Param("workflowId") Long workflowId,
                                                                          @Param("tenantId") Long tenantId);
}
