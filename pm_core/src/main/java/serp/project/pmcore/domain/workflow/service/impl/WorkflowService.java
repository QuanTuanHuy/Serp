/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.domain.screen.port.IScreenPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.enums.WorkflowLifecycleState;
import serp.project.pmcore.domain.shared.enums.WorkflowVersionState;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowVersionEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowSchemePort;
import serp.project.pmcore.domain.workflow.service.IWorkflowService;
import serp.project.pmcore.domain.workflow.entity.WorkflowEntity;
import serp.project.pmcore.domain.workflow.entity.WorkflowStepEntity;
import serp.project.pmcore.domain.workflow.port.IWorkflowPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowStepPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionPort;
import serp.project.pmcore.domain.workflow.port.IWorkflowTransitionRulePort;
import serp.project.pmcore.domain.workflow.port.IWorkflowVersionPort;
import serp.project.pmcore.domain.workflow.query.WorkflowListCriteria;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.service.IStatusService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService implements IWorkflowService {

    private static final int WORKFLOW_NAME_MAX_LENGTH = 255;
    private static final int WORKFLOW_DESCRIPTION_MAX_LENGTH = 2000;
    private static final int WORKFLOW_KEY_MAX_LENGTH = 100;
    private static final int WORKFLOW_TRANSITION_NAME_MAX_LENGTH = 255;

    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IWorkflowTransitionPort workflowTransitionPort;
    private final IWorkflowTransitionRulePort workflowTransitionRulePort;
    private final IScreenPort screenPort;
    private final IStatusService statusService;

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
    public WorkflowStepEntity addWorkflowStep(Long workflowId,
                                              Long statusId,
                                              Boolean isInitial,
                                              Boolean isTerminal,
                                              Long tenantId,
                                              Long userId) {
        WorkflowEntity workflow = getEditableWorkflow(workflowId, tenantId);
        Long draftVersionId = requireDraftVersionId(workflow);
        List<WorkflowStepEntity> existingSteps = workflowStepPort.getWorkflowStepsByWorkflowVersionId(draftVersionId, tenantId);
        StatusEntity status = statusService.getVisibleStatusById(statusId, tenantId);

        boolean duplicateStatus = existingSteps.stream()
                .anyMatch(step -> statusId.equals(step.getStatusId()));
        if (duplicateStatus) {
            throw new BusinessRuleViolationException(DomainErrorCode.WORKFLOW_STEP_DUPLICATE_STATUS);
        }

        if (Boolean.TRUE.equals(isInitial)) {
            boolean hasInitialStep = existingSteps.stream().anyMatch(step -> Boolean.TRUE.equals(step.getIsInitial()));
            if (hasInitialStep) {
                throw new BusinessRuleViolationException(DomainErrorCode.WORKFLOW_MULTIPLE_INITIAL_STEPS);
            }
        }

        long now = System.currentTimeMillis();
        WorkflowStepEntity step = WorkflowStepEntity.builder()
                .tenantId(tenantId)
                .workflowVersionId(draftVersionId)
                .stepKey(status.getStatusKey())
                .name(status.getName())
                .statusId(status.getId())
                .stepOrder(existingSteps.size() + 1)
                .isInitial(Boolean.TRUE.equals(isInitial))
                .isTerminal(Boolean.TRUE.equals(isTerminal))
                .deletedAt(null)
                .build();
        step.applyCreate(userId, now);

        return workflowStepPort.createWorkflowSteps(List.of(step)).getFirst();
    }

    @Override
    public WorkflowStepEntity removeWorkflowStep(Long workflowId,
                                                 Long stepId,
                                                 Long tenantId,
                                                 Long userId) {
        WorkflowEntity workflow = getEditableWorkflow(workflowId, tenantId);
        Long draftVersionId = requireDraftVersionId(workflow);
        WorkflowStepEntity step = workflowStepPort.getWorkflowStepById(stepId, tenantId)
                .filter(existing -> draftVersionId.equals(existing.getWorkflowVersionId()))
                .orElseThrow(() -> ResourceNotFoundException.workflowStepById(stepId));

        long now = System.currentTimeMillis();
        cleanupTransitionsForStep(draftVersionId, stepId, tenantId, userId, now);

        step.setDeletedAt(now);
        step.applyUpdate(userId, now);
        workflowStepPort.updateWorkflowSteps(List.of(step));
        return step;
    }

    @Override
    public List<WorkflowStepEntity> reorderWorkflowSteps(Long workflowId,
                                                         List<Long> stepIds,
                                                         Long tenantId,
                                                         Long userId) {
        WorkflowEntity workflow = getEditableWorkflow(workflowId, tenantId);
        Long draftVersionId = requireDraftVersionId(workflow);
        List<WorkflowStepEntity> existingSteps = workflowStepPort.getWorkflowStepsByWorkflowVersionId(draftVersionId, tenantId);
        validateReorderInput(existingSteps, stepIds);

        List<WorkflowStepEntity> reordered = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (int index = 0; index < stepIds.size(); index++) {
            Long stepId = stepIds.get(index);
            WorkflowStepEntity step = existingSteps.stream()
                    .filter(candidate -> stepId.equals(candidate.getId()))
                    .findFirst()
                    .orElseThrow(() -> ResourceNotFoundException.workflowStepById(stepId));
            step.setStepOrder(index + 1);
            step.applyUpdate(userId, now);
            reordered.add(step);
        }

        return workflowStepPort.updateWorkflowSteps(reordered);
    }

    @Override
    public WorkflowTransitionEntity addWorkflowTransition(Long workflowId,
                                                          String name,
                                                          Long fromStepId,
                                                          Long toStepId,
                                                          Long screenId,
                                                          Integer sequence,
                                                          Long tenantId,
                                                          Long userId) {
        WorkflowEntity workflow = getEditableWorkflow(workflowId, tenantId);
        Long draftVersionId = requireDraftVersionId(workflow);
        List<WorkflowStepEntity> draftSteps = workflowStepPort.getWorkflowStepsByWorkflowVersionId(draftVersionId, tenantId);
        String normalizedName = TextNormalizationUtils.normalizeRequiredText(
                name,
                "name",
                WORKFLOW_TRANSITION_NAME_MAX_LENGTH
        );

        if (toStepId == null) {
            throw new IllegalArgumentException("toStepId is required");
        }
        if (fromStepId != null) {
            requireStepInDraft(draftSteps, fromStepId);
        }
        requireStepInDraft(draftSteps, toStepId);
        validateTransitionScreen(screenId, tenantId);

        long now = System.currentTimeMillis();
        WorkflowTransitionEntity transition = WorkflowTransitionEntity.builder()
                .tenantId(tenantId)
                .workflowVersionId(draftVersionId)
                .name(normalizedName)
                .fromStepId(fromStepId)
                .toStepId(toStepId)
                .screenId(screenId)
                .sequence(resolveTransitionSequence(sequence, draftVersionId, tenantId))
                .deletedAt(null)
                .build();
        transition.applyCreate(userId, now);

        return workflowTransitionPort.createWorkflowTransitions(List.of(transition)).getFirst();
    }

    @Override
    public WorkflowTransitionEntity updateWorkflowTransition(Long workflowId,
                                                             Long transitionId,
                                                             String name,
                                                             Long screenId,
                                                             Integer sequence,
                                                             Long tenantId,
                                                             Long userId) {
        WorkflowEntity workflow = getEditableWorkflow(workflowId, tenantId);
        Long draftVersionId = requireDraftVersionId(workflow);
        WorkflowTransitionEntity transition = workflowTransitionPort
                .getWorkflowTransitionByIdAndWorkflowVersionId(transitionId, draftVersionId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflowTransitionById(transitionId));

        transition.setName(TextNormalizationUtils.normalizeRequiredText(
                name,
                "name",
                WORKFLOW_TRANSITION_NAME_MAX_LENGTH
        ));
        validateTransitionScreen(screenId, tenantId);
        transition.setScreenId(screenId);
        if (sequence != null) {
            transition.setSequence(validateTransitionSequence(sequence));
        }

        long now = System.currentTimeMillis();
        transition.applyUpdate(userId, now);
        return workflowTransitionPort.updateWorkflowTransitions(List.of(transition)).getFirst();
    }

    @Override
    public WorkflowTransitionEntity removeWorkflowTransition(Long workflowId,
                                                             Long transitionId,
                                                             Long tenantId,
                                                             Long userId) {
        WorkflowEntity workflow = getEditableWorkflow(workflowId, tenantId);
        Long draftVersionId = requireDraftVersionId(workflow);
        WorkflowTransitionEntity transition = workflowTransitionPort
                .getWorkflowTransitionByIdAndWorkflowVersionId(transitionId, draftVersionId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflowTransitionById(transitionId));

        long now = System.currentTimeMillis();
        softDeleteTransitionRules(List.of(transition), tenantId, userId, now);
        transition.setDeletedAt(now);
        transition.applyUpdate(userId, now);
        workflowTransitionPort.updateWorkflowTransitions(List.of(transition));
        return transition;
    }

    @Override
    public List<WorkflowTransitionEntity> listWorkflowTransitions(Long workflowId,
                                                                  Long fromStepId,
                                                                  Long tenantId) {
        WorkflowEntity workflow = getEditableWorkflow(workflowId, tenantId);
        Long draftVersionId = requireDraftVersionId(workflow);
        if (fromStepId == null) {
            return workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionId(draftVersionId, tenantId);
        }

        List<WorkflowStepEntity> draftSteps = workflowStepPort.getWorkflowStepsByWorkflowVersionId(draftVersionId, tenantId);
        requireStepInDraft(draftSteps, fromStepId);
        return workflowTransitionPort.getWorkflowTransitionsByWorkflowVersionIdAndFromStepId(draftVersionId, fromStepId, tenantId);
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

    private WorkflowEntity getEditableWorkflow(Long workflowId, Long tenantId) {
        return workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflow(workflowId));
    }

    private Long requireDraftVersionId(WorkflowEntity workflow) {
        if (workflow.getDraftVersionId() == null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.WORKFLOW_DRAFT_NOT_FOUND,
                    "Workflow has no editable draft version: id=" + workflow.getId()
            );
        }
        return workflow.getDraftVersionId();
    }

    private void cleanupTransitionsForStep(Long workflowVersionId,
                                           Long stepId,
                                           Long tenantId,
                                           Long userId,
                                           long now) {
        List<WorkflowTransitionEntity> relatedTransitions = workflowTransitionPort
                .getWorkflowTransitionsByWorkflowVersionId(workflowVersionId, tenantId)
                .stream()
                .filter(transition -> stepId.equals(transition.getFromStepId()) || stepId.equals(transition.getToStepId()))
                .toList();

        if (relatedTransitions.isEmpty()) {
            return;
        }

        for (WorkflowTransitionEntity transition : relatedTransitions) {
            transition.setDeletedAt(now);
            transition.applyUpdate(userId, now);
        }

        softDeleteTransitionRules(relatedTransitions, tenantId, userId, now);
        workflowTransitionPort.updateWorkflowTransitions(relatedTransitions);
    }

    private WorkflowStepEntity requireStepInDraft(List<WorkflowStepEntity> draftSteps, Long stepId) {
        return draftSteps.stream()
                .filter(step -> Objects.equals(step.getId(), stepId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.workflowStepById(stepId));
    }

    private void validateTransitionScreen(Long screenId, Long tenantId) {
        if (screenId == null) {
            return;
        }

        screenPort.getScreenById(screenId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.screen(screenId));
    }

    private Integer resolveTransitionSequence(Integer sequence, Long workflowVersionId, Long tenantId) {
        if (sequence != null) {
            return validateTransitionSequence(sequence);
        }

        List<WorkflowTransitionEntity> existingTransitions = workflowTransitionPort
                .getWorkflowTransitionsByWorkflowVersionId(workflowVersionId, tenantId);
        return existingTransitions.stream()
                .map(WorkflowTransitionEntity::getSequence)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private Integer validateTransitionSequence(Integer sequence) {
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must be greater than or equal to 0");
        }
        return sequence;
    }

    private void softDeleteTransitionRules(List<WorkflowTransitionEntity> transitions,
                                           Long tenantId,
                                           Long userId,
                                           long now) {
        List<WorkflowTransitionRuleEntity> rulesToDelete = new ArrayList<>();
        for (WorkflowTransitionEntity transition : transitions) {
            List<WorkflowTransitionRuleEntity> rules = workflowTransitionRulePort
                    .getWorkflowTransitionRulesByTransitionId(transition.getId(), tenantId)
                    .stream()
                    .toList();
            for (WorkflowTransitionRuleEntity rule : rules) {
                rule.setDeletedAt(now);
                rule.applyUpdate(userId, now);
                rulesToDelete.add(rule);
            }
        }

        if (!rulesToDelete.isEmpty()) {
            workflowTransitionRulePort.updateWorkflowTransitionRules(rulesToDelete);
        }
    }

    private void validateReorderInput(List<WorkflowStepEntity> existingSteps, List<Long> stepIds) {
        if (stepIds == null || stepIds.isEmpty()) {
            throw new IllegalArgumentException("stepIds must not be empty");
        }

        Set<Long> distinctIds = new LinkedHashSet<>(stepIds);
        if (distinctIds.size() != stepIds.size()) {
            throw new IllegalArgumentException("stepIds must contain distinct values");
        }

        Set<Long> existingIds = existingSteps.stream()
                .map(WorkflowStepEntity::getId)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (!distinctIds.equals(existingIds)) {
            throw new IllegalArgumentException("stepIds must match the current workflow steps exactly");
        }
    }

}
