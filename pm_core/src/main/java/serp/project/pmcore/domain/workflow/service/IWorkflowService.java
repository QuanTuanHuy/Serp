/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;

import java.util.List;

public interface IWorkflowService {

    WorkflowEntity createWorkflow(WorkflowEntity workflow, Long tenantId, Long userId);

    WorkflowEntity getVisibleWorkflowById(Long workflowId, Long tenantId);

    PageResult<WorkflowEntity> listVisibleWorkflows(Long tenantId, WorkflowListCriteria criteria);

    WorkflowStepEntity addWorkflowStep(Long workflowId,
                                       Long statusId,
                                       Boolean isInitial,
                                       Boolean isTerminal,
                                       Long tenantId,
                                       Long userId);

    WorkflowStepEntity removeWorkflowStep(Long workflowId,
                                          Long stepId,
                                          Long tenantId,
                                          Long userId);

    List<WorkflowStepEntity> reorderWorkflowSteps(Long workflowId,
                                                  List<Long> stepIds,
                                                  Long tenantId,
                                                  Long userId);

    WorkflowEntity resolveWorkflow(Long workflowSchemeId,
                                   Long issueTypeId,
                                   Long tenantId);

    WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId);
}
