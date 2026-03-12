/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.WorkflowTransitionEntity;

import java.util.List;

public interface IWorkflowTransitionPort {
    List<WorkflowTransitionEntity> createWorkflowTransitions(List<WorkflowTransitionEntity> transitions);

    List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowIdIncludingSystem(Long workflowId, Long tenantId);
}
