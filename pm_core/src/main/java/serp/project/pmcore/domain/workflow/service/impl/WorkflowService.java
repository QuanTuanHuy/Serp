/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import org.springframework.stereotype.Service;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService implements IWorkflowService {

    private static final int WORKFLOW_NAME_MAX_LENGTH = 255;
    private static final int WORKFLOW_DESCRIPTION_MAX_LENGTH = 2000;
    private static final int WORKFLOW_KEY_MAX_LENGTH = 100;

    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;
    private final IWorkflowStepPort workflowStepPort;

    @Override
    public WorkflowEntity createWorkflow(WorkflowEntity workflow, Long tenantId, Long userId) {
        String name = TextNormalizationUtils.normalizeRequiredText(
                workflow.getName(),
                "name",
                WORKFLOW_NAME_MAX_LENGTH
        );
        long now = System.currentTimeMillis();

        WorkflowEntity draftWorkflow = WorkflowEntity.builder()
                .tenantId(tenantId)
                .workflowKey(generateWorkflowKey(name, tenantId))
                .name(name)
                .description(TextNormalizationUtils.normalizeOptionalText(
                        workflow.getDescription(),
                        "description",
                        WORKFLOW_DESCRIPTION_MAX_LENGTH
                ))
                .currentPublishedVersionId(null)
                .draftVersionId(null)
                .lifecycleState(WorkflowLifecycleState.INACTIVE)
                .isSystem(false)
                .build();
        draftWorkflow.applyCreate(userId, now);

        WorkflowEntity createdWorkflow = workflowPort.createWorkflow(draftWorkflow);

        WorkflowVersionEntity draftVersion = WorkflowVersionEntity.builder()
                .tenantId(tenantId)
                .workflowId(createdWorkflow.getId())
                .versionNo(1)
                .versionState(WorkflowVersionState.DRAFT)
                .baseVersionId(null)
                .publishedAt(null)
                .publishedBy(null)
                .build();
        draftVersion.applyCreate(userId, now);
        WorkflowVersionEntity createdDraftVersion = workflowVersionPort.createWorkflowVersion(draftVersion);

        createdWorkflow.setDraftVersionId(createdDraftVersion.getId());
        createdWorkflow.applyUpdate(userId, now);
        workflowPort.updateWorkflow(createdWorkflow);
        return createdWorkflow;
    }

    @Override
    public WorkflowEntity getVisibleWorkflowById(Long workflowId, Long tenantId) {
        return workflowPort.getWorkflowByIdIncludingSystem(workflowId, tenantId)
                .orElseThrow(() -> {
                    log.error("Visible workflow not found: id={}, tenantId={}", workflowId, tenantId);
                    return ResourceNotFoundException.workflow(workflowId);
                });
    }

    @Override
    public PageResult<WorkflowEntity> listVisibleWorkflows(Long tenantId, WorkflowListCriteria criteria) {
        return workflowPort.listWorkflowsIncludingSystem(tenantId, criteria);
    }

    @Override
    public WorkflowEntity resolveWorkflow(Long workflowSchemeId, Long issueTypeId, Long tenantId) {
        if (workflowSchemeId == null) {
            log.error("[WorkflowService] Workflow scheme id is null");
            throw new ResourceNotFoundException(
                    DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                    "Project has no workflow scheme binding"
            );
        }

        WorkflowSchemeEntity scheme = workflowSchemePort.getWorkflowSchemeById(workflowSchemeId, tenantId)
                .orElseThrow(() -> {
                    log.error("[WorkflowService] Workflow scheme not found: id={}, tenantId={}", workflowSchemeId, tenantId);
                    return new ResourceNotFoundException(
                            DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                            "Workflow scheme not found: id=" + workflowSchemeId
                    );
                });

        Long workflowId = workflowSchemeItemPort
                .getItemBySchemeIdAndIssueTypeId(workflowSchemeId, issueTypeId, tenantId)
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .orElse(scheme.getDefaultWorkflowId());
        if (workflowId == null) {
            log.error("[WorkflowService] No workflow found for issue type id={}, workflow scheme id={}",
                    issueTypeId, workflowSchemeId);
            throw new ResourceNotFoundException(
                    DomainErrorCode.WORKFLOW_NOT_FOUND,
                    "No workflow found for issue type id=" + issueTypeId + " in workflow scheme id=" + workflowSchemeId
            );
        }

        return workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> {
                    log.error("[WorkflowService] Workflow not found: id={}, tenantId={}", workflowId, tenantId);
                    return new ResourceNotFoundException(
                            DomainErrorCode.WORKFLOW_NOT_FOUND,
                            "Workflow not found: id=" + workflowId
                    );
                });
    }

    @Override
    public WorkflowStepEntity getInitialWorkflowStep(Long workflowId, Long tenantId) {
        WorkflowEntity workflow = workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_NOT_FOUND,
                        "Workflow not found: id=" + workflowId
                ));

        if (workflow.getCurrentPublishedVersionId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.WORKFLOW_STEP_NOT_FOUND,
                    "Workflow step not found: id=" + workflowId
            );
        }

        return workflowStepPort.getInitialStepByWorkflowVersionId(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_STEP_NOT_FOUND,
                        "Initial workflow step not found for workflow id=" + workflowId
                ));
    }

    private String generateWorkflowKey(String name, Long tenantId) {
        String normalized = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        String baseKey = normalized.isEmpty()
                ? "workflow"
                : trimToMaxLength(normalized, WORKFLOW_KEY_MAX_LENGTH);
        String candidate = baseKey;
        int suffix = 2;

        while (workflowPort.getWorkflowByWorkflowKey(tenantId, candidate).isPresent()) {
            String suffixValue = "_" + suffix;
            candidate = trimToMaxLength(baseKey, WORKFLOW_KEY_MAX_LENGTH - suffixValue.length()) + suffixValue;
            suffix++;
        }

        return candidate;
    }

    private String trimToMaxLength(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

}
