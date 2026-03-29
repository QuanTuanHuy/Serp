/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;

import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;

public interface IWorkflowTransitionPort {
    List<WorkflowTransitionEntity> createWorkflowTransitions(List<WorkflowTransitionEntity> transitions);

    List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionId(Long workflowVersionId, Long tenantId);

    List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionIdIncludingSystem(Long workflowVersionId, Long tenantId);
}
