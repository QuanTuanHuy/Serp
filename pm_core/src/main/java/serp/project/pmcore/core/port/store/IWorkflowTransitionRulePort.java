/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.WorkflowTransitionRuleEntity;

import java.util.List;

public interface IWorkflowTransitionRulePort {
    List<WorkflowTransitionRuleEntity> createWorkflowTransitionRules(List<WorkflowTransitionRuleEntity> rules);

    List<WorkflowTransitionRuleEntity> getWorkflowTransitionRulesByTransitionIdIncludingSystem(Long transitionId, Long tenantId);
}
