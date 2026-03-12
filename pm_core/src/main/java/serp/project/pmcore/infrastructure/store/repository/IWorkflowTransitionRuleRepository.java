/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.WorkflowTransitionRuleModel;

import java.util.List;

@Repository
public interface IWorkflowTransitionRuleRepository extends JpaRepository<WorkflowTransitionRuleModel, Long> {

    @Query("SELECT r FROM WorkflowTransitionRuleModel r WHERE r.transitionId = :transitionId " +
           "AND (r.tenantId = :tenantId OR r.tenantId = 0) ORDER BY r.sequence ASC, r.id ASC")
    List<WorkflowTransitionRuleModel> findByTransitionIdAndTenantIdOrSystemTenant(
            @Param("transitionId") Long transitionId, @Param("tenantId") Long tenantId);
}
