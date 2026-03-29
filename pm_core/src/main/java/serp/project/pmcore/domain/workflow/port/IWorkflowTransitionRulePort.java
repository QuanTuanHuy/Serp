/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;

import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;

public interface IWorkflowTransitionRulePort {
    List<WorkflowTransitionRuleEntity> createWorkflowTransitionRules(List<WorkflowTransitionRuleEntity> rules);

    List<WorkflowTransitionRuleEntity> getWorkflowTransitionRulesByTransitionIdIncludingSystem(Long transitionId, Long tenantId);
}
