/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.workflow.WorkflowEntity;

import java.util.List;

public interface IWorkflowTransitionPort {
    List<WorkflowEntity.WorkflowTransitionEntity> createWorkflowTransitions(List<WorkflowEntity.WorkflowTransitionEntity> transitions);

    List<WorkflowEntity.WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowIdIncludingSystem(Long workflowId, Long tenantId);
}
