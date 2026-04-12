/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workitem.port.IStatusPort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WorkflowSchemeCompatibilityValidator {

    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;
    private final IIssueTypePort issueTypePort;
    private final IStatusPort statusPort;
    private final IScreenPort screenPort;

    public void validate(Long workflowSchemeId, Set<Long> issueTypeIds, Long tenantId) {
        WorkflowSchemeEntity workflowScheme = workflowSchemePort.getWorkflowSchemeById(workflowSchemeId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                        "Workflow scheme not found in tenant scope: id=" + workflowSchemeId
                ));

        List<WorkflowSchemeItemEntity> schemeItems = workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeId(workflowSchemeId, tenantId);
        Map<Long, Long> issueTypeWorkflowMap = new HashMap<>();
        for (WorkflowSchemeItemEntity item : schemeItems) {
            issueTypePort.getIssueTypeById(item.getIssueTypeId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.ISSUE_TYPE_NOT_FOUND,
                            "Workflow scheme references issue type outside tenant scope: issueTypeId=" + item.getIssueTypeId()
                    ));
            issueTypeWorkflowMap.put(item.getIssueTypeId(), item.getWorkflowId());
        }

        Set<Long> effectiveWorkflowIds = new HashSet<>();
        for (Long issueTypeId : issueTypeIds) {
            Long workflowId = issueTypeWorkflowMap.get(issueTypeId);
            if (workflowId == null) {
                workflowId = workflowScheme.getDefaultWorkflowId();
            }

            if (workflowId == null) {
                throw new DomainValidationException(
                        DomainErrorCode.WORKFLOW_SCHEME_COVERAGE_MISSING,
                        "Workflow scheme does not cover issue type id=" + issueTypeId
                );
            }

            effectiveWorkflowIds.add(workflowId);
        }
        if (workflowScheme.getDefaultWorkflowId() != null) {
            effectiveWorkflowIds.add(workflowScheme.getDefaultWorkflowId());
        }

        for (Long workflowId : effectiveWorkflowIds) {
            validateWorkflowPublicationAndInitialStep(workflowId, tenantId);
        }
    }

    private void validateWorkflowPublicationAndInitialStep(Long workflowId, Long tenantId) {
        WorkflowEntity workflow = workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORKFLOW_NOT_FOUND,
                        "Workflow not found in tenant scope: id=" + workflowId
                ));

        if (workflow.getCurrentPublishedVersionId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow has no current published version: id=" + workflowId
            );
        }

        WorkflowVersionEntity publishedVersion = workflowVersionPort
                .getWorkflowVersionById(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                        "Published workflow version not found in tenant scope: id="
                                + workflow.getCurrentPublishedVersionId()
                ));

        if (!publishedVersion.isActive()) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow current version is not published: workflowId=" + workflowId
                            + ", versionId=" + publishedVersion.getId()
            );
        }

        List<WorkflowStepEntity> steps = workflowStepPort
                .getWorkflowStepsByWorkflowVersionId(publishedVersion.getId(), tenantId);
        long initialSteps = steps.stream()
                .filter(step -> Boolean.TRUE.equals(step.getIsInitial()))
                .count();
        if (initialSteps == 0) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NO_INITIAL_STEP,
                    "Published workflow version has no initial step: versionId=" + publishedVersion.getId()
            );
        }
        if (initialSteps > 1) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_MULTIPLE_INITIAL_STEPS,
                    "Published workflow version has multiple initial steps: versionId=" + publishedVersion.getId()
            );
        }

        Set<Long> stepIds = new HashSet<>();
        for (WorkflowStepEntity step : steps) {
            stepIds.add(step.getId());
            statusPort.getStatusById(step.getStatusId(), tenantId)
                    .orElseThrow(() -> new DomainValidationException(
                            DomainErrorCode.STATUS_NOT_FOUND,
                            "Workflow step references status outside tenant scope: statusId=" + step.getStatusId()
                    ));
        }

        List<WorkflowTransitionEntity> transitions = workflowTransitionPort
                .getWorkflowTransitionsByWorkflowVersionId(publishedVersion.getId(), tenantId);
        for (WorkflowTransitionEntity transition : transitions) {
            if (transition.getFromStepId() != null && !stepIds.contains(transition.getFromStepId())) {
                throw new DomainValidationException(
                        DomainErrorCode.SCHEME_INCOMPATIBLE,
                        "Workflow transition references fromStep outside published version: transitionId=" + transition.getId()
                );
            }

            if (transition.getToStepId() == null || !stepIds.contains(transition.getToStepId())) {
                throw new DomainValidationException(
                        DomainErrorCode.SCHEME_INCOMPATIBLE,
                        "Workflow transition references invalid toStep for published version: transitionId=" + transition.getId()
                );
            }

            if (transition.getScreenId() != null) {
                screenPort.getScreenById(transition.getScreenId(), tenantId)
                        .orElseThrow(() -> new DomainValidationException(
                                DomainErrorCode.SCREEN_NOT_FOUND,
                                "Workflow transition screen not found in tenant scope: screenId=" + transition.getScreenId()
                        ));
            }
        }
    }
}
