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

    @Query("SELECT s FROM WorkflowStepModel s WHERE s.workflowVersionId = :workflowVersionId " +
           "AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    List<WorkflowStepModel> findByWorkflowVersionIdAndTenantIdOrSystemTenant(
            @Param("workflowVersionId") Long workflowVersionId, @Param("tenantId") Long tenantId);
    
    List<WorkflowStepModel> findByWorkflowVersionIdAndTenantId(Long workflowVersionId, Long tenantId);

    @Query("SELECT s FROM WorkflowStepModel s WHERE s.workflowVersionId = :workflowVersionId " +
           "AND s.isInitial = true AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    Optional<WorkflowStepModel> findInitialStepByWorkflowVersionId(
            @Param("workflowVersionId") Long workflowVersionId, @Param("tenantId") Long tenantId);

    Optional<WorkflowStepModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT COUNT(s) > 0 FROM WorkflowStepModel s WHERE s.statusId = :statusId AND (s.tenantId = :tenantId OR s.tenantId = 0)")
    boolean existsByStatusIdAndTenantIdOrSystemTenant(@Param("statusId") Long statusId, @Param("tenantId") Long tenantId);
}
