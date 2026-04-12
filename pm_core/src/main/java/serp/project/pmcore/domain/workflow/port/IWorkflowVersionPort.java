/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;

public interface IWorkflowVersionPort {
    WorkflowVersionEntity createWorkflowVersion(WorkflowVersionEntity workflowVersion);

    void updateWorkflowVersion(WorkflowVersionEntity workflowVersion);

    List<WorkflowVersionEntity> createWorkflowVersions(List<WorkflowVersionEntity> workflowVersions);

    Optional<WorkflowVersionEntity> getWorkflowVersionById(Long id, Long tenantId);

    Optional<WorkflowVersionEntity> getWorkflowVersionByIdIncludingSystem(Long id, Long tenantId);

    List<WorkflowVersionEntity> getWorkflowVersionsByWorkflowIdIncludingSystem(Long workflowId, Long tenantId);
}
