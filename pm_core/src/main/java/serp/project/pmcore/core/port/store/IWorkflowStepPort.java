/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.WorkflowStepEntity;

import java.util.List;
import java.util.Optional;

public interface IWorkflowStepPort {

    List<WorkflowStepEntity> getWorkflowStepsByWorkflowId(Long workflowId, Long tenantId);

    Optional<WorkflowStepEntity> getInitialStep(Long workflowId, Long tenantId);
}
