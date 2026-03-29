/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;

public interface IWorkflowStepPort {

    List<WorkflowStepEntity> createWorkflowSteps(List<WorkflowStepEntity> steps);

    List<WorkflowStepEntity> getWorkflowStepsByWorkflowVersionId(Long workflowVersionId, Long tenantId);

    List<WorkflowStepEntity> getWorkflowStepsByWorkflowVersionIdIncludingSystem(Long workflowVersionId, Long tenantId);

    Optional<WorkflowStepEntity> getInitialStepByWorkflowVersionId(Long workflowVersionId, Long tenantId);
}
