/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionRuleEntity;

import java.util.List;

public interface IWorkflowTransitionRulePort {
    List<WorkflowTransitionRuleEntity> createWorkflowTransitionRules(List<WorkflowTransitionRuleEntity> rules);

    List<WorkflowTransitionRuleEntity> getWorkflowTransitionRulesByTransitionIdIncludingSystem(Long transitionId, Long tenantId);
}
