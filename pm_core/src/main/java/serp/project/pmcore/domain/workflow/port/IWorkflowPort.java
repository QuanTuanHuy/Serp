/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.Optional;

import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;

public interface IWorkflowPort {
    WorkflowEntity createWorkflow(WorkflowEntity workflow);

    void updateWorkflow(WorkflowEntity workflow);

    Optional<WorkflowEntity> getWorkflowById(Long id, Long tenantId);

    Optional<WorkflowEntity> getWorkflowByIdIncludingSystem(Long id, Long tenantId);
}
