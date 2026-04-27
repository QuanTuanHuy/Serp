/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workflow.dto.WorkflowValidationResult;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
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

    WorkflowTransitionEntity addWorkflowTransition(Long workflowId,
                                                   String name,
                                                   Long fromStepId,
                                                   Long toStepId,
                                                   Long screenId,
                                                   Integer sequence,
                                                   Long tenantId,
                                                   Long userId);

    WorkflowTransitionEntity updateWorkflowTransition(Long workflowId,
                                                      Long transitionId,
                                                      String name,
                                                      Long screenId,
                                                      Integer sequence,
                                                      Long tenantId,
                                                      Long userId);

    WorkflowTransitionEntity removeWorkflowTransition(Long workflowId,
                                                      Long transitionId,
                                                      Long tenantId,
                                                      Long userId);

    List<WorkflowTransitionEntity> listWorkflowTransitions(Long workflowId,
                                                           Long fromStepId,
                                                           Long tenantId);

    WorkflowValidationResult validateWorkflow(Long workflowId, Long tenantId);

    WorkflowEntity publishWorkflow(Long workflowId, Long tenantId, Long userId);

    WorkflowEntity resolveWorkflow(Long workflowSchemeId,
                                   Long issueTypeId,
                                   Long tenantId);

    WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId);
}
