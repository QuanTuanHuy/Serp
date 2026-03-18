/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;

import java.util.List;
import java.util.Optional;

public interface IWorkflowStepPort {

    List<WorkflowStepEntity> createWorkflowSteps(List<WorkflowStepEntity> steps);

    List<WorkflowStepEntity> getWorkflowStepsByWorkflowId(Long workflowId, Long tenantId);

    List<WorkflowStepEntity> getWorkflowStepsByWorkflowIdIncludingSystem(Long workflowId, Long tenantId);

    Optional<WorkflowStepEntity> getInitialStep(Long workflowId, Long tenantId);
}
