/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;

public interface IWorkflowService {

    WorkflowEntity createWorkflow(WorkflowEntity workflow, Long tenantId, Long userId);

    WorkflowEntity getVisibleWorkflowById(Long workflowId, Long tenantId);

    PageResult<WorkflowEntity> listVisibleWorkflows(Long tenantId, WorkflowListCriteria criteria);

    WorkflowEntity resolveWorkflow(Long workflowSchemeId,
                                   Long issueTypeId,
                                   Long tenantId);

    WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId);
}
