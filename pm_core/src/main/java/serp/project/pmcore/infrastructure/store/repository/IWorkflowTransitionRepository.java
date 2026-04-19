/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkflowTransitionModel;

import java.util.List;

@Repository
public interface IWorkflowTransitionRepository extends JpaRepository<WorkflowTransitionModel, Long> {

    List<WorkflowTransitionModel> findByWorkflowVersionIdAndTenantIdOrderBySequenceAscIdAsc(Long workflowVersionId,
                                                                                             Long tenantId);

    List<WorkflowTransitionModel> findByWorkflowVersionIdAndFromStepIdAndTenantIdOrderBySequenceAscIdAsc(
            Long workflowVersionId,
            Long fromStepId,
            Long tenantId);

    @Query("SELECT t FROM WorkflowTransitionModel t WHERE t.workflowVersionId = :workflowVersionId " +
           "AND (t.tenantId = :tenantId OR t.tenantId = 0) ORDER BY t.sequence ASC, t.id ASC")
    List<WorkflowTransitionModel> findByWorkflowVersionIdAndTenantIdOrSystemTenant(
            @Param("workflowVersionId") Long workflowVersionId, @Param("tenantId") Long tenantId);
}
