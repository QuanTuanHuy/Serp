/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkflowStepModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWorkflowStepRepository extends JpaRepository<WorkflowStepModel, Long> {

    @Query("SELECT s FROM WorkflowStepModel s WHERE s.workflowId = :workflowId " +
           "AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    List<WorkflowStepModel> findByWorkflowIdAndTenantIdOrSystemTenant(
            @Param("workflowId") Long workflowId, @Param("tenantId") Long tenantId);

    @Query("SELECT s FROM WorkflowStepModel s WHERE s.workflowId = :workflowId " +
           "AND s.isInitial = true AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<WorkflowStepModel> findInitialStepByWorkflowId(
            @Param("workflowId") Long workflowId, @Param("tenantId") Long tenantId);
}
