/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.transition;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.command.transition.internal.ResolvedTransitionExecution;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionSubjectContext;
import serp.project.pmcore.application.workitem.command.transition.internal.TransitionWorkItemStatusData;
import serp.project.pmcore.application.workitem.command.transition.support.TransitionConfigurationResolver;
import serp.project.pmcore.application.workitem.history.WorkItemHistoryRecorder;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.dto.ResolvedCustomFields;
import serp.project.pmcore.domain.customfield.port.ICustomFieldPort;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldResolver;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.shared.util.WorkItemFieldUtils;
import serp.project.pmcore.domain.shared.util.WorkItemFieldValueUtils;
import serp.project.pmcore.domain.workflow.entity.WorkflowTransitionRuleEntity;
import serp.project.pmcore.domain.workflow.service.IWorkItemTransitionRuleEvaluator;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemFieldResolver;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.workitem.service.IWorkItemTransitionAuthorizationService;
import serp.project.pmcore.domain.workitem.validator.WorkItemScheduleValidator;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransitionWorkItemCommandHandler
        implements ICommandHandler<TransitionWorkItemStatusCommand, TransitionWorkItemStatusResult> {

    private static final String WORKFLOW_STEP_ID = "workflow_step_id";
    private static final String STATUS_ID = "status_id";

    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IWorkItemTransitionAuthorizationService workItemTransitionAuthorizationService;
    private final TransitionConfigurationResolver transitionConfigurationResolver;
    private final IWorkItemTransitionRuleEvaluator workItemTransitionRuleEvaluator;
    private final IWorkItemFieldResolver workItemFieldResolver;
    private final IWorkItemCustomFieldResolver workItemCustomFieldResolver;
    private final ICustomFieldPort customFieldPort;
    private final IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;
    private final TransitionWorkItemStatusValidator transitionWorkItemStatusValidator;
    private final WorkItemHistoryRecorder workItemHistoryRecorder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TransitionWorkItemStatusResult handle(TransitionWorkItemStatusCommand command) {
        transitionWorkItemStatusValidator.validate(command);

        Long tenantId = command.tenantId();
        Long userId = command.userId();
        TransitionWorkItemStatusData data = command.toData();

        ProjectEntity project = projectService.getProjectById(command.projectId(), tenantId);
        ensureProjectWritable(project);

        WorkItemEntity workItem = workItemService.getWorkItemById(command.workItemId(), tenantId);
        ensureWorkItemBelongsToProject(workItem, project);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                userId,
                command.groupKeys(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
        workItemTransitionAuthorizationService.checkTransitionPermissions(project, actorContext);
        workItemTransitionAuthorizationService.checkIssueSecurityAccessIfNeeded(project, workItem, actorContext, tenantId);

        ResolvedTransitionExecution execution = transitionConfigurationResolver.resolve(
                new TransitionSubjectContext(
                        project.getId(),
                        project.getWorkflowSchemeId(),
                        workItem.getId(),
                        workItem.getIssueTypeId(),
                        workItem.getWorkflowStepId(),
                        workItem.getStatusId()
                ),
                command.transitionId(),
                tenantId
        );

        List<WorkItemCustomFieldValueEntity> existingCustomFieldValues = workItemCustomFieldValuePort
                .getActiveValuesByWorkItemId(workItem.getId(), tenantId);
        WorkItemFieldRules fieldRules = resolveFieldRules(execution, project, workItem, tenantId);
        Map<String, CustomFieldEntity> customFieldsByKey = loadCustomFieldsByKey(collectRelevantCustomFieldKeys(execution, fieldRules, data));
        Map<String, Object> existingCustomFieldValuesByKey = mapExistingCustomFieldValues(customFieldsByKey, existingCustomFieldValues);

        workItemTransitionRuleEvaluator.evaluateConditions(execution, workItem, project, userId);

        Long candidateResolutionId = workItemTransitionRuleEvaluator.evaluateValidatorsAndResolveResolution(
                execution,
                workItem,
                data,
                command.resolutionId(),
                existingCustomFieldValuesByKey,
                tenantId
        );

        validateTransitionFieldPayload(execution, data, fieldRules);
        workItemTransitionAuthorizationService.checkFieldLevelPermissions(project, actorContext, data);

        Long resolvedAssigneeId = workItemTransitionAuthorizationService.resolveAssigneeId(
                project,
                workItem.getAssigneeId(),
                actorContext,
                data
        );
        Long resolvedSecurityLevelId = workItemTransitionAuthorizationService.resolveSecurityLevelId(
                workItem.getSecurityLevelId(),
                project.getIssueSecuritySchemeId(),
                data,
                tenantId
        );

        ResolvedCustomFields resolvedCustomFields = resolveCustomFields(
                execution,
                data,
                fieldRules,
                existingCustomFieldValuesByKey
        );
        validateRequiredFields(
                workItem,
                data,
                fieldRules,
                resolvedCustomFields,
                resolvedAssigneeId,
                resolvedSecurityLevelId
        );
        validateScheduleRange(workItem, data);

        Map<String, Object> originalSnapshot = snapshotTrackedFields(workItem);

        applySystemFieldUpdates(workItem, data, resolvedAssigneeId, resolvedSecurityLevelId);
        workItem.setWorkflowStepId(execution.targetStep().getId());
        workItem.setStatusId(execution.targetStatus().getId());
        workItem.setResolutionId(candidateResolutionId);

        workItemTransitionRuleEvaluator.applyPostFunctions(execution, workItem, command.resolutionId());

        WorkItemEntity updatedWorkItem = workItemService.updateWorkItem(workItem, userId);
        persistCustomFieldValues(
                updatedWorkItem.getId(),
                data.customFields().keySet(),
                customFieldsByKey,
                resolvedCustomFields.values(),
                tenantId,
                userId
        );

        List<String> changedFields = buildChangedFields(originalSnapshot, updatedWorkItem, data.customFields().keySet());
        workItemHistoryRecorder.recordChanges(
                tenantId,
                updatedWorkItem.getId(),
                userId,
                originalSnapshot,
                snapshotTrackedFields(updatedWorkItem),
                changedFields
        );
        persistStatusChangedOutboxEvent(
                updatedWorkItem,
                project.getId(),
                execution,
                changedFields,
                tenantId,
                userId
        );

        log.info("Transitioned work item id={} projectId={} transitionId={} fromStepId={} toStepId={}",
                updatedWorkItem.getId(),
                project.getId(),
                execution.transition().getId(),
                execution.currentStep().getId(),
                execution.targetStep().getId());

        return new TransitionWorkItemStatusResult(
                updatedWorkItem.getId(),
                updatedWorkItem.getProjectId(),
                updatedWorkItem.getKey(),
                updatedWorkItem.getSummary(),
                updatedWorkItem.getDescription(),
                updatedWorkItem.getWorkflowStepId(),
                updatedWorkItem.getStatusId(),
                updatedWorkItem.getAssigneeId(),
                updatedWorkItem.getResolutionId(),
                updatedWorkItem.getSecurityLevelId(),
                updatedWorkItem.getDueDate(),
                updatedWorkItem.getTimeOriginalEstimate(),
                execution.transition().getId(),
                execution.transition().getName(),
                execution.currentStep().getId(),
                execution.targetStep().getId(),
                changedFields,
                updatedWorkItem.getUpdatedAt(),
                userId
        );
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private void ensureWorkItemBelongsToProject(WorkItemEntity workItem, ProjectEntity project) {
        if (!Objects.equals(workItem.getProjectId(), project.getId())) {
            throw new ResourceNotFoundException(DomainErrorCode.WORK_ITEM_NOT_FOUND);
        }
    }

    private WorkItemFieldRules resolveFieldRules(ResolvedTransitionExecution execution,
                                                 ProjectEntity project,
                                                 WorkItemEntity workItem,
                                                 Long tenantId) {
        if (execution.transition().getScreenId() == null) {
            return WorkItemFieldRules.empty();
        }

        return workItemFieldResolver.resolveFieldRules(
                project.getId(),
                project.getFieldConfigSchemeId(),
                workItem.getIssueTypeId(),
                execution.transition().getScreenId(),
                tenantId
        );
    }

    private Map<String, CustomFieldEntity> loadCustomFieldsByKey(Collection<String> fieldKeys) {
        if (fieldKeys == null || fieldKeys.isEmpty()) {
            return Map.of();
        }

        return customFieldPort.getCustomFieldsByFieldKeys(new ArrayList<>(new LinkedHashSet<>(fieldKeys)))
                .stream()
                .collect(Collectors.toMap(
                        CustomFieldEntity::getFieldKey,
                        field -> field,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private Set<String> collectRelevantCustomFieldKeys(ResolvedTransitionExecution execution,
                                                       WorkItemFieldRules fieldRules,
                                                       TransitionWorkItemStatusData data) {
        LinkedHashSet<String> fieldKeys = new LinkedHashSet<>(fieldRules.customPolicies().keySet());
        fieldKeys.addAll(data.customFields().keySet());

        for (WorkflowTransitionRuleEntity rule : execution.rules()) {
            String fieldRef = extractConfiguredFieldRef(rule);
            if (isCustomFieldRef(fieldRef)) {
                fieldKeys.add(fieldRef);
            }
        }

        return fieldKeys;
    }

    private String extractConfiguredFieldRef(WorkflowTransitionRuleEntity rule) {
        if (rule.getConfigJson() == null || rule.getConfigJson().isBlank()) {
            return null;
        }

        Map<String, Object> config = jsonUtils.fromJson(rule.getConfigJson(), new TypeReference<>() {
        });
        Object rawFieldRef = config.get("field");
        if (rawFieldRef == null) rawFieldRef = config.get("fieldRef");
        if (rawFieldRef == null) rawFieldRef = config.get("field_key");
        if (!(rawFieldRef instanceof String text) || text.isBlank()) {
            return null;
        }

        return WorkItemFieldUtils.normalizeFieldRef(text);
    }

    private boolean isCustomFieldRef(String fieldRef) {
        if (fieldRef == null || fieldRef.isBlank()) {
            return false;
        }

        return !WorkItemFieldConstants.SUPPORTED_TRANSITION_SYSTEM_FIELDS.contains(fieldRef)
                && !WorkItemFieldConstants.RESOLUTION_ID.equals(fieldRef);
    }

    private Map<String, Object> mapExistingCustomFieldValues(Map<String, CustomFieldEntity> customFieldsByKey,
                                                             List<WorkItemCustomFieldValueEntity> existingValues) {
        if (customFieldsByKey.isEmpty() || existingValues == null || existingValues.isEmpty()) {
            return Map.of();
        }

        Set<Long> activeCustomFieldIds = existingValues.stream()
                .map(WorkItemCustomFieldValueEntity::getCustomFieldId)
                .collect(Collectors.toSet());

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, CustomFieldEntity> entry : customFieldsByKey.entrySet()) {
            if (activeCustomFieldIds.contains(entry.getValue().getId())) {
                result.put(entry.getKey(), Boolean.TRUE);
            }
        }
        return result;
    }

    private void validateTransitionFieldPayload(ResolvedTransitionExecution execution,
                                                TransitionWorkItemStatusData data,
                                                WorkItemFieldRules fieldRules) {
        if (execution.transition().getScreenId() == null) {
            if (!data.fields().isEmpty()) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.TRANSITION_FIELD_INVALID,
                        "Transition does not expose a screen for field updates: transitionId=" + execution.transition().getId()
                );
            }
            return;
        }

        for (Map.Entry<String, Object> entry : data.systemFields().entrySet()) {
            WorkItemFieldPolicy policy = fieldRules.getSystemFieldPolicy(entry.getKey());
            if (policy == null || !policy.isClientWritable()) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.TRANSITION_FIELD_INVALID,
                        "System field is not writable on transition screen: field=" + entry.getKey()
                );
            }
            validateSystemFieldValue(entry.getKey(), entry.getValue());
        }

        for (String customFieldKey : data.customFields().keySet()) {
            WorkItemFieldPolicy policy = fieldRules.getCustomFieldPolicy(customFieldKey);
            if (policy == null || !policy.isClientWritable()) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.TRANSITION_FIELD_INVALID,
                        "Custom field is not writable on transition screen: field=" + customFieldKey
                );
            }
        }
    }

    private void validateSystemFieldValue(String fieldRef, Object rawValue) {
        try {
            switch (fieldRef) {
                case WorkItemFieldConstants.SUMMARY, WorkItemFieldConstants.DESCRIPTION -> WorkItemFieldValueUtils.asNullableString(rawValue);
                case WorkItemFieldConstants.PRIORITY_ID,
                     WorkItemFieldConstants.ASSIGNEE_ID,
                     WorkItemFieldConstants.SECURITY_LEVEL_ID -> WorkItemFieldValueUtils.asNullablePositiveLong(rawValue);
                case WorkItemFieldConstants.DUE_DATE,
                     WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE,
                     WorkItemFieldConstants.TIME_REMAINING_ESTIMATE -> WorkItemFieldValueUtils.asNullableNonNegativeLong(rawValue);
                default -> throw new BusinessRuleViolationException(
                        DomainErrorCode.TRANSITION_FIELD_INVALID,
                        "Unsupported transition system field: field=" + fieldRef
                );
            }
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.TRANSITION_FIELD_INVALID,
                    "Invalid transition field value: field=" + fieldRef + ", message=" + ex.getMessage()
            );
        }
    }

    private ResolvedCustomFields resolveCustomFields(ResolvedTransitionExecution execution,
                                                     TransitionWorkItemStatusData data,
                                                     WorkItemFieldRules fieldRules,
                                                     Map<String, Object> existingCustomFieldValuesByKey) {
        if (data.customFields().isEmpty()) {
            return ResolvedCustomFields.empty();
        }

        Map<String, Object> nonNullCustomFields = data.customFields().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        Map<String, Boolean> requiredCustomFields = fieldRules.customPolicies().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().required(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        ResolvedCustomFields resolvedCustomFields = workItemCustomFieldResolver.resolveCustomFields(
                execution.issueType().getTypeKey(),
                nonNullCustomFields,
                requiredCustomFields
        );

        List<String> filteredMissingFields = resolvedCustomFields.missingFields().stream()
                .filter(fieldKey -> !(existingCustomFieldValuesByKey.containsKey(fieldKey)
                        && !data.customFields().containsKey(fieldKey)))
                .toList();

        return new ResolvedCustomFields(resolvedCustomFields.values(), filteredMissingFields);
    }

    private void validateRequiredFields(WorkItemEntity workItem,
                                        TransitionWorkItemStatusData data,
                                        WorkItemFieldRules fieldRules,
                                        ResolvedCustomFields resolvedCustomFields,
                                        Long resolvedAssigneeId,
                                        Long resolvedSecurityLevelId) {
        List<String> missingFields = new ArrayList<>();

        Map<String, Object> effectiveSystemValues = new LinkedHashMap<>();
        effectiveSystemValues.put(WorkItemFieldConstants.SUMMARY,
                data.hasSystemField(WorkItemFieldConstants.SUMMARY)
                        ? WorkItemFieldValueUtils.asNullableString(data.getSystemField(WorkItemFieldConstants.SUMMARY))
                        : workItem.getSummary());
        effectiveSystemValues.put(WorkItemFieldConstants.DESCRIPTION,
                data.hasSystemField(WorkItemFieldConstants.DESCRIPTION)
                        ? WorkItemFieldValueUtils.asNullableString(data.getSystemField(WorkItemFieldConstants.DESCRIPTION))
                        : workItem.getDescription());
        effectiveSystemValues.put(WorkItemFieldConstants.PRIORITY_ID,
                data.hasSystemField(WorkItemFieldConstants.PRIORITY_ID)
                        ? WorkItemFieldValueUtils.asNullablePositiveLong(data.getSystemField(WorkItemFieldConstants.PRIORITY_ID))
                        : workItem.getPriorityId());
        effectiveSystemValues.put(WorkItemFieldConstants.ASSIGNEE_ID,
                data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)
                        ? resolvedAssigneeId
                        : workItem.getAssigneeId());
        effectiveSystemValues.put(WorkItemFieldConstants.START_DATE, workItem.getStartDate());
        effectiveSystemValues.put(WorkItemFieldConstants.DUE_DATE,
                data.hasSystemField(WorkItemFieldConstants.DUE_DATE)
                        ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE))
                        : workItem.getDueDate());
        effectiveSystemValues.put(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE,
                data.hasSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE)
                        ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE))
                        : workItem.getTimeOriginalEstimate());
        effectiveSystemValues.put(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE,
                data.hasSystemField(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE)
                        ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE))
                        : workItem.getTimeRemainingEstimate());
        effectiveSystemValues.put(WorkItemFieldConstants.SECURITY_LEVEL_ID,
                data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)
                        ? resolvedSecurityLevelId
                        : workItem.getSecurityLevelId());

        for (WorkItemFieldPolicy policy : fieldRules.systemPolicies().values()) {
            if (!policy.required() || !WorkItemFieldConstants.SUPPORTED_TRANSITION_SYSTEM_FIELDS.contains(policy.fieldRef())) {
                continue;
            }

            if (isMissingValue(effectiveSystemValues.get(policy.fieldRef()))) {
                missingFields.add(policy.fieldRef());
            }
        }

        missingFields.addAll(resolvedCustomFields.missingFields());
        if (!missingFields.isEmpty()) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.REQUIRED_FIELDS_MISSING,
                    "Required fields are missing: " + String.join(", ", missingFields)
            );
        }
    }

    private boolean isMissingValue(Object value) {
        if (value == null) {
            return true;
        }
        return value instanceof String text && text.isBlank();
    }

    private Map<String, Object> snapshotTrackedFields(WorkItemEntity workItem) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put(WorkItemFieldConstants.SUMMARY, workItem.getSummary());
        snapshot.put(WorkItemFieldConstants.DESCRIPTION, workItem.getDescription());
        snapshot.put(WorkItemFieldConstants.PRIORITY_ID, workItem.getPriorityId());
        snapshot.put(WorkItemFieldConstants.ASSIGNEE_ID, workItem.getAssigneeId());
        snapshot.put(WorkItemFieldConstants.START_DATE, workItem.getStartDate());
        snapshot.put(WorkItemFieldConstants.DUE_DATE, workItem.getDueDate());
        snapshot.put(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, workItem.getTimeOriginalEstimate());
        snapshot.put(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE, workItem.getTimeRemainingEstimate());
        snapshot.put(WorkItemFieldConstants.SECURITY_LEVEL_ID, workItem.getSecurityLevelId());
        snapshot.put(WorkItemFieldConstants.RESOLUTION_ID, workItem.getResolutionId());
        snapshot.put(WORKFLOW_STEP_ID, workItem.getWorkflowStepId());
        snapshot.put(STATUS_ID, workItem.getStatusId());
        return snapshot;
    }

    private void applySystemFieldUpdates(WorkItemEntity workItem,
                                         TransitionWorkItemStatusData data,
                                         Long resolvedAssigneeId,
                                         Long resolvedSecurityLevelId) {
        if (data.hasSystemField(WorkItemFieldConstants.SUMMARY)) {
            workItem.setSummary(WorkItemFieldValueUtils.asNullableString(data.getSystemField(WorkItemFieldConstants.SUMMARY)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.DESCRIPTION)) {
            workItem.setDescription(WorkItemFieldValueUtils.asNullableString(data.getSystemField(WorkItemFieldConstants.DESCRIPTION)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.PRIORITY_ID)) {
            workItem.setPriorityId(WorkItemFieldValueUtils.asNullablePositiveLong(data.getSystemField(WorkItemFieldConstants.PRIORITY_ID)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)) {
            workItem.setAssigneeId(resolvedAssigneeId);
        }
        if (data.hasSystemField(WorkItemFieldConstants.DUE_DATE)) {
            workItem.setDueDate(WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE)) {
            workItem.setTimeOriginalEstimate(WorkItemFieldValueUtils.asNullableNonNegativeLong(
                    data.getSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE)) {
            workItem.setTimeRemainingEstimate(WorkItemFieldValueUtils.asNullableNonNegativeLong(
                    data.getSystemField(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            workItem.setSecurityLevelId(resolvedSecurityLevelId);
        }
    }

    private List<String> buildChangedFields(Map<String, Object> originalSnapshot,
                                            WorkItemEntity updatedWorkItem,
                                            Set<String> requestedCustomFields) {
        List<String> changedFields = new ArrayList<>();
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.SUMMARY, updatedWorkItem.getSummary());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.DESCRIPTION, updatedWorkItem.getDescription());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.PRIORITY_ID, updatedWorkItem.getPriorityId());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.ASSIGNEE_ID, updatedWorkItem.getAssigneeId());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.START_DATE, updatedWorkItem.getStartDate());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.DUE_DATE, updatedWorkItem.getDueDate());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, updatedWorkItem.getTimeOriginalEstimate());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.TIME_REMAINING_ESTIMATE, updatedWorkItem.getTimeRemainingEstimate());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.SECURITY_LEVEL_ID, updatedWorkItem.getSecurityLevelId());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.RESOLUTION_ID, updatedWorkItem.getResolutionId());
        addIfChanged(changedFields, originalSnapshot, WORKFLOW_STEP_ID, updatedWorkItem.getWorkflowStepId());
        addIfChanged(changedFields, originalSnapshot, STATUS_ID, updatedWorkItem.getStatusId());

        if (requestedCustomFields != null && !requestedCustomFields.isEmpty()) {
            changedFields.addAll(requestedCustomFields.stream().sorted().toList());
        }

        return changedFields;
    }

    private void addIfChanged(List<String> changedFields,
                              Map<String, Object> originalSnapshot,
                              String fieldRef,
                              Object newValue) {
        if (!Objects.equals(originalSnapshot.get(fieldRef), newValue)) {
            changedFields.add(fieldRef);
        }
    }

    private void validateScheduleRange(WorkItemEntity workItem, TransitionWorkItemStatusData data) {
        Long effectiveDueDate = data.hasSystemField(WorkItemFieldConstants.DUE_DATE)
                ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE))
                : workItem.getDueDate();
        WorkItemScheduleValidator.validateRange(workItem.getStartDate(), effectiveDueDate);
    }

    private void persistCustomFieldValues(Long workItemId,
                                          Set<String> requestedCustomFieldKeys,
                                          Map<String, CustomFieldEntity> customFieldsByKey,
                                          List<WorkItemCustomFieldValueEntity> values,
                                          Long tenantId,
                                          Long userId) {
        if (requestedCustomFieldKeys == null || requestedCustomFieldKeys.isEmpty()) {
            return;
        }

        List<Long> requestedCustomFieldIds = requestedCustomFieldKeys.stream()
                .map(customFieldsByKey::get)
                .filter(Objects::nonNull)
                .map(CustomFieldEntity::getId)
                .distinct()
                .toList();
        long now = System.currentTimeMillis();
        workItemCustomFieldValuePort.softDeleteByWorkItemIdAndCustomFieldIds(workItemId, requestedCustomFieldIds, userId, now);

        if (values == null || values.isEmpty()) {
            return;
        }

        for (WorkItemCustomFieldValueEntity value : values) {
            value.setWorkItemId(workItemId);
            value.setTenantId(tenantId);
            value.applyCreate(userId, now);
        }
        workItemCustomFieldValuePort.saveAll(values);
    }

    private void persistStatusChangedOutboxEvent(WorkItemEntity updatedWorkItem,
                                                 Long projectId,
                                                 ResolvedTransitionExecution execution,
                                                 List<String> changedFields,
                                                 Long tenantId,
                                                 Long userId) {
        WorkItemEventPayload payload = WorkItemEventPayload.builder()
                .workItemId(updatedWorkItem.getId())
                .workItemKey(updatedWorkItem.getKey())
                .projectId(projectId)
                .issueTypeId(updatedWorkItem.getIssueTypeId())
                .statusId(updatedWorkItem.getStatusId())
                .assigneeId(updatedWorkItem.getAssigneeId())
                .transitionId(execution.transition().getId())
                .transitionName(execution.transition().getName())
                .fromStepId(execution.currentStep().getId())
                .toStepId(execution.targetStep().getId())
                .resolutionId(updatedWorkItem.getResolutionId())
                .transitionedAt(updatedWorkItem.getUpdatedAt())
                .transitionedBy(userId)
                .changedFields(changedFields)
                .build();

        Long eventTime = updatedWorkItem.getUpdatedAt() == null ? System.currentTimeMillis() : updatedWorkItem.getUpdatedAt();
        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.WorkItem.AGGREGATE)
                .aggregateId(updatedWorkItem.getId())
                .eventType(EventConstants.WorkItem.EventType.WORK_ITEM_STATUS_CHANGED)
                .topic(EventConstants.WorkItem.TOPIC)
                .partitionKey(String.valueOf(projectId))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(eventTime)
                .updatedAt(eventTime)
                .build();
        outboxEventService.saveEvent(outboxEvent);
    }

}
