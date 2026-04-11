/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workflow.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.workitem.command.transition.internal.ResolvedTransitionExecution;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.enums.TransitionRuleStage;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.util.WorkItemFieldUtils;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.workflow.service.IWorkItemTransitionRuleEvaluator;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IResolutionPort;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemTransitionRuleEvaluator implements IWorkItemTransitionRuleEvaluator {

    private final JsonUtils jsonUtils;
    private final IResolutionPort resolutionPort;

    @Override
    public void evaluateConditions(ResolvedTransitionExecution execution,
                                   WorkItemEntity workItem,
                                   ProjectEntity project,
                                   Long userId) {
        for (WorkflowTransitionRuleEntity rule : rulesByStage(execution, TransitionRuleStage.CONDITION)) {
            String ruleKey = normalizeToken(rule.getRuleKey());
            switch (ruleKey) {
                case "user_is_assignee" -> {
                    if (userId == null || !userId.equals(workItem.getAssigneeId())) {
                        throw new BusinessRuleViolationException(
                                DomainErrorCode.TRANSITION_CONDITION_FAILED,
                                "Transition condition failed: user must be assignee, transitionId=" + execution.transition().getId()
                        );
                    }
                }
                case "user_is_reporter" -> {
                    if (userId == null || !userId.equals(workItem.getReporterId())) {
                        throw new BusinessRuleViolationException(
                                DomainErrorCode.TRANSITION_CONDITION_FAILED,
                                "Transition condition failed: user must be reporter, transitionId=" + execution.transition().getId()
                        );
                    }
                }
                case "user_is_project_lead" -> {
                    if (userId == null || !userId.equals(project.getLeadUserId())) {
                        throw new BusinessRuleViolationException(
                                DomainErrorCode.TRANSITION_CONDITION_FAILED,
                                "Transition condition failed: user must be project lead, transitionId=" + execution.transition().getId()
                        );
                    }
                }
                default -> throw new DomainValidationException(
                        DomainErrorCode.WORKFLOW_VALIDATION_FAILED,
                        "Unsupported transition condition rule: ruleKey=" + rule.getRuleKey()
                );
            }
        }
    }

    @Override
    public Long evaluateValidatorsAndResolveResolution(ResolvedTransitionExecution execution,
                                                       WorkItemEntity workItem,
                                                       TransitionWorkItemStatusData data,
                                                       Long requestedResolutionId,
                                                       Map<String, Object> existingCustomFieldValues,
                                                       Long tenantId) {
        Long candidateResolutionId = requestedResolutionId;
        boolean resolutionRequired = "done".equalsIgnoreCase(execution.targetStatusCategory().getKey());

        for (WorkflowTransitionRuleEntity rule : rulesByStage(execution, TransitionRuleStage.VALIDATOR)) {
            String ruleKey = normalizeToken(rule.getRuleKey());
            Map<String, Object> config = parseRuleConfig(rule);

            switch (ruleKey) {
                case "resolution_required" -> resolutionRequired = true;
                case "field_required" -> {
                    String fieldRef = extractFieldRef(config);
                    if (fieldRef == null) {
                        throw new DomainValidationException(
                                DomainErrorCode.WORKFLOW_VALIDATION_FAILED,
                                "field_required validator requires field config: ruleId=" + rule.getId()
                        );
                    }
                    Object candidateValue = resolveCandidateFieldValue(
                            fieldRef,
                            workItem,
                            data,
                            candidateResolutionId,
                            existingCustomFieldValues
                    );
                    if (isEmpty(candidateValue)) {
                        throw new BusinessRuleViolationException(
                                DomainErrorCode.TRANSITION_VALIDATION_FAILED,
                                "Required field missing during transition: field=" + fieldRef
                        );
                    }
                }
                default -> throw new DomainValidationException(
                        DomainErrorCode.WORKFLOW_VALIDATION_FAILED,
                        "Unsupported transition validator rule: ruleKey=" + rule.getRuleKey()
                );
            }
        }

        if (resolutionRequired && candidateResolutionId == null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.RESOLUTION_REQUIRED,
                    "Resolution is required for transition to done category: transitionId=" + execution.transition().getId()
            );
        }

        if (candidateResolutionId != null) {
            resolutionPort.getResolutionByIdIncludingSystem(candidateResolutionId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            DomainErrorCode.RESOLUTION_NOT_FOUND,
                            "Resolution not found: id=" + candidateResolutionId
                    ));
        }

        return candidateResolutionId;
    }

    @Override
    public void applyPostFunctions(ResolvedTransitionExecution execution,
                                   WorkItemEntity candidate,
                                   Long requestedResolutionId) {
        for (WorkflowTransitionRuleEntity rule : rulesByStage(execution, TransitionRuleStage.POST_FUNCTION)) {
            String ruleKey = normalizeToken(rule.getRuleKey());
            switch (ruleKey) {
                case "clear_resolution" -> candidate.setResolutionId(null);
                case "set_resolution_from_request" -> {
                    if (requestedResolutionId == null) {
                        throw new BusinessRuleViolationException(
                                DomainErrorCode.RESOLUTION_REQUIRED,
                                "Transition post-function requires resolution from request: transitionId=" + execution.transition().getId()
                        );
                    }
                    candidate.setResolutionId(requestedResolutionId);
                }
                case "fire_event" -> {
                    // No-op here. Outbox event is persisted by handler after aggregate mutation.
                }
                default -> throw new DomainValidationException(
                        DomainErrorCode.WORKFLOW_VALIDATION_FAILED,
                        "Unsupported transition post-function rule: ruleKey=" + rule.getRuleKey()
                );
            }
        }
    }

    private List<WorkflowTransitionRuleEntity> rulesByStage(ResolvedTransitionExecution execution,
                                                            TransitionRuleStage stage) {
        return execution.rules().stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getIsEnabled()))
                .filter(rule -> stage.equals(rule.getRuleStage()))
                .sorted(Comparator
                        .comparing((WorkflowTransitionRuleEntity rule) -> rule.getSequence() == null ? Integer.MAX_VALUE : rule.getSequence())
                        .thenComparing(rule -> rule.getId() == null ? Long.MAX_VALUE : rule.getId()))
                .toList();
    }

    private Map<String, Object> parseRuleConfig(WorkflowTransitionRuleEntity rule) {
        if (rule.getConfigJson() == null || rule.getConfigJson().isBlank()) {
            return Map.of();
        }
        return jsonUtils.fromJson(rule.getConfigJson(), new TypeReference<>() {
        });
    }

    private String extractFieldRef(Map<String, Object> config) {
        Object raw = config.get("field");
        if (raw == null) raw = config.get("fieldRef");
        if (raw == null) raw = config.get("field_key");
        if (!(raw instanceof String text) || text.isBlank()) {
            return null;
        }

        return WorkItemFieldUtils.normalizeFieldRef(text);
    }

    private Object resolveCandidateFieldValue(String fieldRef,
                                              WorkItemEntity workItem,
                                              TransitionWorkItemStatusData data,
                                              Long candidateResolutionId,
                                              Map<String, Object> existingCustomFieldValues) {
        return switch (fieldRef) {
            case WorkItemFieldConstants.SUMMARY ->
                    data.hasSystemField(fieldRef) ? data.getSystemField(fieldRef) : workItem.getSummary();
            case WorkItemFieldConstants.DESCRIPTION ->
                    data.hasSystemField(fieldRef) ? data.getSystemField(fieldRef) : workItem.getDescription();
            case WorkItemFieldConstants.PRIORITY_ID ->
                    data.hasSystemField(fieldRef) ? data.getSystemField(fieldRef) : workItem.getPriorityId();
            case WorkItemFieldConstants.ASSIGNEE_ID ->
                    data.hasSystemField(fieldRef) ? data.getSystemField(fieldRef) : workItem.getAssigneeId();
            case WorkItemFieldConstants.DUE_DATE ->
                    data.hasSystemField(fieldRef) ? data.getSystemField(fieldRef) : workItem.getDueDate();
            case WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE ->
                    data.hasSystemField(fieldRef) ? data.getSystemField(fieldRef) : workItem.getTimeOriginalEstimate();
            case WorkItemFieldConstants.SECURITY_LEVEL_ID ->
                    data.hasSystemField(fieldRef) ? data.getSystemField(fieldRef) : workItem.getSecurityLevelId();
            case WorkItemFieldConstants.RESOLUTION_ID ->
                    candidateResolutionId != null ? candidateResolutionId : workItem.getResolutionId();
            default -> {
                if (data.customFields().containsKey(fieldRef)) {
                    yield data.customFields().get(fieldRef);
                }

                if (existingCustomFieldValues != null && existingCustomFieldValues.containsKey(fieldRef)) {
                    yield existingCustomFieldValues.get(fieldRef);
                }

                throw new DomainValidationException(
                        DomainErrorCode.WORKFLOW_VALIDATION_FAILED,
                        "Unsupported validator field reference: field=" + fieldRef
                );
            }
        };
    }

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        return value instanceof String text && text.isBlank();
    }

    private String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }
}
