/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.workflow.WorkflowTransitionEntity;

import java.util.List;

public interface IWorkflowTransitionPort {
    List<WorkflowTransitionEntity> createWorkflowTransitions(List<WorkflowTransitionEntity> transitions);

    List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionIdIncludingSystem(Long workflowVersionId, Long tenantId);
}
