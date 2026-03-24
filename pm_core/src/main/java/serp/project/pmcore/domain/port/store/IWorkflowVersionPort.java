/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.workflow.WorkflowVersionEntity;

import java.util.List;
import java.util.Optional;

public interface IWorkflowVersionPort {
    WorkflowVersionEntity createWorkflowVersion(WorkflowVersionEntity workflowVersion);

    void updateWorkflowVersion(WorkflowVersionEntity workflowVersion);

    List<WorkflowVersionEntity> createWorkflowVersions(List<WorkflowVersionEntity> workflowVersions);

    Optional<WorkflowVersionEntity> getWorkflowVersionById(Long id, Long tenantId);

    Optional<WorkflowVersionEntity> getWorkflowVersionByIdIncludingSystem(Long id, Long tenantId);

    List<WorkflowVersionEntity> getWorkflowVersionsByWorkflowIdIncludingSystem(Long workflowId, Long tenantId);
}
