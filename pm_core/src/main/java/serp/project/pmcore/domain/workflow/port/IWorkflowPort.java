/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.port;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;

import java.util.Optional;

import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;

public interface IWorkflowPort {
    WorkflowEntity createWorkflow(WorkflowEntity workflow);

    void updateWorkflow(WorkflowEntity workflow);

    Optional<WorkflowEntity> getWorkflowById(Long id, Long tenantId);

    Optional<WorkflowEntity> getWorkflowByIdIncludingSystem(Long id, Long tenantId);

    Optional<WorkflowEntity> getWorkflowByWorkflowKey(Long tenantId, String workflowKey);

    PageResult<WorkflowEntity> listWorkflowsIncludingSystem(Long tenantId, WorkflowListCriteria criteria);
}
