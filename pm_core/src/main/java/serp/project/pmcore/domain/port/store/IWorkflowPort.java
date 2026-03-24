/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.workflow.WorkflowEntity;

import java.util.Optional;

public interface IWorkflowPort {
    WorkflowEntity createWorkflow(WorkflowEntity workflow);

    void updateWorkflow(WorkflowEntity workflow);

    Optional<WorkflowEntity> getWorkflowById(Long id, Long tenantId);

    Optional<WorkflowEntity> getWorkflowByIdIncludingSystem(Long id, Long tenantId);
}
