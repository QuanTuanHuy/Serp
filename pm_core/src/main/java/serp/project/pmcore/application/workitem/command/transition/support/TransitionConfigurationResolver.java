/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.transition.internal.ResolvedTransitionExecution;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workflow.entity.*;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionRulePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransitionConfigurationResolver {

    private final IIssueTypeService issueTypeService;
    private final IWorkflowService workflowService;
    private final IStatusService statusService;

    private final IWorkflowVersionPort workflowVersionPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;
    private final IWorkflowTransitionRulePort workflowTransitionRulePort;

    public ResolvedTransitionExecution resolve(ProjectEntity project,
                                               WorkItemEntity workItem,
                                               Long transitionId,
                                               Long tenantId) {
        IssueTypeEntity issueType = issueTypeService.getIssueTypeById(workItem.getIssueTypeId(), tenantId);

        WorkflowEntity workflow;
        try {
            workflow = workflowService.resolveWorkflow(project.getWorkflowSchemeId(), workItem.getIssueTypeId(), tenantId);
        } catch (ResourceNotFoundException ex) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_RESOLVABLE,
                    "Effective workflow cannot be resolved for work item: workItemId=" + workItem.getId()
            );
        }

        WorkflowVersionEntity workflowVersion = resolveWorkflowVersion(
                workflow.getId(), workflow.getCurrentPublishedVersionId(), tenantId);

        WorkflowStepEntity currentStep = resolveWorkflowStep(
                workItem.getId(),
                workItem.getWorkflowStepId(),
                workflowVersion.getId(),
                tenantId
        );

        if (!Objects.equals(currentStep.getStatusId(), workItem.getStatusId())) {
            throw new DomainValidationException(
                    DomainErrorCode.WORK_ITEM_WORKFLOW_STATE_INVALID,
                    "Work item status does not match workflow step: workItemId=" + workItem.getId()
                            + ", workflowStepId=" + currentStep.getId()
                            + ", workItemStatusId=" + workItem.getStatusId()
                            + ", expectedStatusId=" + currentStep.getStatusId()
            );
        }

        WorkflowTransitionEntity transition = workflowTransitionPort
                .getWorkflowTransitionByIdAndWorkflowVersionId(transitionId, workflowVersion.getId(), tenantId)
                .filter(t -> t.getFromStepId() == null ||
                        t.getFromStepId().equals(currentStep.getId()))
                .orElseThrow(() -> {
                    List<WorkflowTransitionEntity> availableTransitions = workflowTransitionPort
                            .getWorkflowTransitionsByWorkflowVersionIdIncludingSystem(workflowVersion.getId(), tenantId)
                            .stream()
                            .filter(t -> t.getFromStepId() == null || t.getFromStepId().equals(currentStep.getId()))
                            .toList();
                    log.warn("Invalid transition requested: transitionId={}, workflowVersionId={}, currentStepId={}, availableTransitionIds={}",
                            transitionId,
                            workflowVersion.getId(),
                            currentStep.getId(),
                            availableTransitions.stream().map(WorkflowTransitionEntity::getId).toList());
                    return new BusinessRuleViolationException(
                            DomainErrorCode.INVALID_TRANSITION,
                            "Transition is not available from current step: transitionId=" + transitionId
                                    + ", availableTransitionIds=" + availableTransitions.stream().map(WorkflowTransitionEntity::getId).toList()
                    );
                });

        List<WorkflowTransitionRuleEntity> rules = workflowTransitionRulePort
                .getWorkflowTransitionRulesByTransitionIdIncludingSystem(transitionId, tenantId);

        WorkflowStepEntity targetStep = resolveWorkflowStep(
                workItem.getId(),
                transition.getToStepId(),
                workflowVersion.getId(),
                tenantId
        );

        StatusEntity targetStatus;
        try {
            targetStatus = statusService.getStatusById(targetStep.getStatusId(), tenantId);
        } catch (ResourceNotFoundException ex) {
            throw new DomainValidationException(
                    DomainErrorCode.WORK_ITEM_WORKFLOW_STATE_INVALID,
                    "Target workflow step references missing status: stepId=" + targetStep.getId()
                            + ", statusId=" + targetStep.getStatusId()
            );
        }

        StatusCategoryEntity targetStatusCategory = resolveStatusCategory(targetStatus.getCategoryId(), targetStatus.getId(), tenantId);

        return new ResolvedTransitionExecution(
                issueType,
                workflowVersion,
                currentStep,
                targetStep,
                transition,
                rules,
                targetStatus,
                targetStatusCategory
        );
    }

    private WorkflowVersionEntity resolveWorkflowVersion(Long workflowId,
                                                         Long workflowVersionId,
                                                         Long tenantId) {
        if (workflowVersionId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_RESOLVABLE,
                    "Workflow has no published version: workflowId=" + workflowId
            );
        }

        WorkflowVersionEntity workflowVersion = workflowVersionPort
                .getWorkflowVersionById(workflowVersionId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORKFLOW_NOT_RESOLVABLE,
                        "Published workflow version not found: id=" + workflowVersionId
                ));

        if (!workflowVersion.isActive()) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_RESOLVABLE,
                    "Workflow current version is not published: workflowId=" + workflowId
                            + ", versionId=" + workflowVersion.getId()
            );
        }
        return workflowVersion;
    }

    private WorkflowStepEntity resolveWorkflowStep(Long workItemId,
                                                   Long stepId,
                                                   Long workflowVersionId,
                                                   Long tenantId) {
        if (stepId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORK_ITEM_WORKFLOW_STATE_INVALID,
                    "Work item has no workflow_step_id: workItemId=" + workItemId
            );
        }

        WorkflowStepEntity step = workflowStepPort.getWorkflowStepById(stepId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORK_ITEM_WORKFLOW_STATE_INVALID,
                        "Workflow step not found: stepId=" + stepId
                ));

        if (!Objects.equals(step.getWorkflowVersionId(), workflowVersionId)) {
            throw new DomainValidationException(
                    DomainErrorCode.WORK_ITEM_WORKFLOW_STATE_INVALID,
                    "Workflow step does not belong to effective workflow version: stepId=" + stepId
                            + ", workflowVersionId=" + workflowVersionId
            );
        }

        return step;
    }

    private StatusCategoryEntity resolveStatusCategory(Long categoryId, Long statusId, Long tenantId) {
        if (categoryId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.STATUS_CATEGORY_NOT_FOUND,
                    "Target status has no category binding: statusId=" + statusId
            );
        }

        try {
            return statusService.getStatusCategoryById(categoryId, tenantId);
        } catch (ResourceNotFoundException ex) {
            throw new DomainValidationException(
                    DomainErrorCode.WORK_ITEM_WORKFLOW_STATE_INVALID,
                    "Target status references missing category: statusId=" + statusId + ", categoryId=" + categoryId
            );
        }
    }
}
