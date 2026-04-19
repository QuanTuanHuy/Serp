/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;

public interface IWorkflowTransitionPort {
    List<WorkflowTransitionEntity> createWorkflowTransitions(List<WorkflowTransitionEntity> transitions);

    List<WorkflowTransitionEntity> updateWorkflowTransitions(List<WorkflowTransitionEntity> transitions);

    List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionId(Long workflowVersionId, Long tenantId);

    List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionIdAndFromStepId(Long workflowVersionId,
                                                                                           Long fromStepId,
                                                                                           Long tenantId);

    List<WorkflowTransitionEntity> getWorkflowTransitionsByWorkflowVersionIdIncludingSystem(Long workflowVersionId, Long tenantId);

    Optional<WorkflowTransitionEntity> getWorkflowTransitionByIdAndWorkflowVersionId(Long id, Long workflowVersionId, Long tenantId);
}
