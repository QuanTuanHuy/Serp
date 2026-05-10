/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.command.roleactor.RoleActorSubjectValidator;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.command.update.internal.UpdateWorkItemData;
import serp.project.pmcore.application.workitem.command.update.support.UpdateWorkItemConfigurationResolver;
import serp.project.pmcore.application.workitem.command.update.support.UpdateWorkItemFieldRulesResolver;
import serp.project.pmcore.application.workitem.command.update.support.UpdateWorkItemFieldWriteValidator;
import serp.project.pmcore.domain.customfield.dto.WorkItemCustomFieldMutationPlan;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldMutationService;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.enums.ProjectRoleActorSubjectType;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.shared.util.WorkItemFieldValueUtils;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.workitem.validator.WorkItemScheduleValidator;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.ArrayList;
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
public class UpdateWorkItemCommandHandler
        implements ICommandHandler<UpdateWorkItemCommand, UpdateWorkItemResult> {

    private final UpdateWorkItemValidator updateWorkItemValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;
    private final RoleActorSubjectValidator roleActorSubjectValidator;
    private final UpdateWorkItemFieldRulesResolver updateWorkItemFieldRulesResolver;
    private final UpdateWorkItemFieldWriteValidator updateWorkItemFieldWriteValidator;
    private final UpdateWorkItemConfigurationResolver updateWorkItemConfigurationResolver;
    private final IWorkItemCustomFieldMutationService workItemCustomFieldMutationService;
    private final IIssueTypePort issueTypePort;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateWorkItemResult handle(UpdateWorkItemCommand command) {
        updateWorkItemValidator.validate(command);

        Long tenantId = command.tenantId();
        Long userId = command.userId();
        UpdateWorkItemData data = command.data();

        ProjectEntity project = projectService.getProjectById(command.projectId(), tenantId);
        ensureProjectWritable(project);
        ProjectPermissionSubject permissionSubject = ProjectPermissionSubject.from(project);

        WorkItemEntity workItem = workItemService.getWorkItemById(command.workItemId(), tenantId);
        ensureWorkItemBelongsToProject(workItem, project);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                userId,
                command.groupKeys(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
        workItemAuthorizationSupportService.checkRequiredPermissions(
                permissionSubject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.EDIT_ISSUES
        );
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);

        WorkItemFieldRules fieldRules = updateWorkItemFieldRulesResolver.resolveEditFieldRules(
                project,
                workItem.getIssueTypeId(),
                tenantId
        );
        updateWorkItemFieldWriteValidator.validateClientSuppliedWritableFields(data, fieldRules);

        checkFieldLevelPermissions(permissionSubject, actorContext, data);
        Long resolvedAssigneeId = resolveAssigneeId(permissionSubject, actorContext, data);
        Long resolvedPriorityId = updateWorkItemConfigurationResolver.resolvePriorityId(
                project.getId(),
                project.getPrioritySchemeId(),
                workItem.getPriorityId(),
                data,
                tenantId
        );
        Long resolvedSecurityLevelId = updateWorkItemConfigurationResolver.resolveSecurityLevelId(
                project.getId(),
                project.getIssueSecuritySchemeId(),
                workItem.getSecurityLevelId(),
                data,
                tenantId
        );

        String issueTypeKey = issueTypePort.getIssueTypeById(workItem.getIssueTypeId(), workItem.getTenantId())
                .orElseThrow(() -> ResourceNotFoundException.issueType(workItem.getIssueTypeId()))
                .getTypeKey();
        WorkItemCustomFieldMutationPlan customFieldPlan = workItemCustomFieldMutationService.planUpdate(
                issueTypeKey,
                workItem.getId(),
                tenantId,
                data.customFields(),
                toRequiredCustomFieldMap(fieldRules)
        );

        validateRequiredFields(
                workItem,
                data,
                fieldRules,
                resolvedPriorityId,
                resolvedAssigneeId,
                resolvedSecurityLevelId,
                customFieldPlan.missingRequiredFields()
        );
        validateScheduleRange(workItem, data);

        Map<String, Object> originalSnapshot = snapshotTrackedFields(workItem);

        applySystemFieldUpdates(workItem, data, resolvedPriorityId, resolvedAssigneeId, resolvedSecurityLevelId);
        WorkItemEntity updatedWorkItem = workItemService.updateWorkItem(workItem, userId);
        workItemCustomFieldMutationService.applyPlan(updatedWorkItem.getId(), tenantId, userId, customFieldPlan);

        List<String> changedFields = buildChangedFields(originalSnapshot, updatedWorkItem, customFieldPlan.changedFieldKeys());
        persistUpdatedOutboxEvent(updatedWorkItem, changedFields, tenantId);

        log.info("Updated work item id={} projectId={} changedFields={}",
                updatedWorkItem.getId(),
                updatedWorkItem.getProjectId(),
                changedFields);

        return UpdateWorkItemResult.from(updatedWorkItem, changedFields);
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

    private void checkFieldLevelPermissions(ProjectPermissionSubject permissionSubject,
                                            ProjectPermissionEvaluationContext actorContext,
                                            UpdateWorkItemData data) {
        if (data.hasSystemField(WorkItemFieldConstants.START_DATE)
                || data.hasSystemField(WorkItemFieldConstants.DUE_DATE)) {
            workItemAuthorizationSupportService.checkScheduleIssuesPermission(permissionSubject, actorContext);
        }
        if (data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)) {
            workItemAuthorizationSupportService.checkRequiredPermissions(permissionSubject, actorContext, ProjectPermissionKeys.ASSIGN_ISSUES);
        }
        if (data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            workItemAuthorizationSupportService.checkRequiredPermissions(permissionSubject, actorContext, ProjectPermissionKeys.SET_ISSUE_SECURITY);
        }
    }

    private Long resolveAssigneeId(ProjectPermissionSubject permissionSubject,
                                   ProjectPermissionEvaluationContext actorContext,
                                   UpdateWorkItemData data) {
        if (!data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)) {
            return null;
        }

        Long requestedAssigneeId = WorkItemFieldValueUtils.asNullablePositiveLong(
                data.getSystemField(WorkItemFieldConstants.ASSIGNEE_ID));
        if (requestedAssigneeId != null) {
            roleActorSubjectValidator.validateSubjectExistsForAdd(
                    ProjectRoleActorSubjectType.USER,
                    String.valueOf(requestedAssigneeId)
            );
        }

        return requestedAssigneeId == null
                ? null
                : workItemAuthorizationSupportService.resolveAssigneeId(permissionSubject, requestedAssigneeId, actorContext);
    }

    private void validateRequiredFields(WorkItemEntity workItem,
                                        UpdateWorkItemData data,
                                        WorkItemFieldRules fieldRules,
                                        Long resolvedPriorityId,
                                        Long resolvedAssigneeId,
                                        Long resolvedSecurityLevelId,
                                        List<String> missingCustomFields) {
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
                        ? resolvedPriorityId
                        : workItem.getPriorityId());
        effectiveSystemValues.put(WorkItemFieldConstants.ASSIGNEE_ID,
                data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)
                        ? resolvedAssigneeId
                        : workItem.getAssigneeId());
        effectiveSystemValues.put(WorkItemFieldConstants.START_DATE,
                data.hasSystemField(WorkItemFieldConstants.START_DATE)
                        ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.START_DATE))
                        : workItem.getStartDate());
        effectiveSystemValues.put(WorkItemFieldConstants.DUE_DATE,
                data.hasSystemField(WorkItemFieldConstants.DUE_DATE)
                        ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE))
                        : workItem.getDueDate());
        effectiveSystemValues.put(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE,
                data.hasSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE)
                        ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE))
                        : workItem.getTimeOriginalEstimate());
        effectiveSystemValues.put(WorkItemFieldConstants.SECURITY_LEVEL_ID,
                data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)
                        ? resolvedSecurityLevelId
                        : workItem.getSecurityLevelId());

        if (isMissingValue(effectiveSystemValues.get(WorkItemFieldConstants.SUMMARY))) {
            missingFields.add(WorkItemFieldConstants.SUMMARY);
        }

        for (WorkItemFieldPolicy policy : fieldRules.systemPolicies().values()) {
            if (!policy.required() || WorkItemFieldConstants.SUMMARY.equals(policy.fieldRef())) {
                continue;
            }
            if (!WorkItemFieldConstants.SUPPORTED_UPDATE_SYSTEM_FIELDS.contains(policy.fieldRef())) {
                continue;
            }
            if (isMissingValue(effectiveSystemValues.get(policy.fieldRef()))) {
                missingFields.add(policy.fieldRef());
            }
        }

        if (missingCustomFields != null && !missingCustomFields.isEmpty()) {
            missingFields.addAll(missingCustomFields);
        }

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
        snapshot.put(WorkItemFieldConstants.SECURITY_LEVEL_ID, workItem.getSecurityLevelId());
        return snapshot;
    }

    private void applySystemFieldUpdates(WorkItemEntity workItem,
                                         UpdateWorkItemData data,
                                         Long resolvedPriorityId,
                                         Long resolvedAssigneeId,
                                         Long resolvedSecurityLevelId) {
        if (data.hasSystemField(WorkItemFieldConstants.SUMMARY)) {
            workItem.setSummary(WorkItemFieldValueUtils.asNullableString(data.getSystemField(WorkItemFieldConstants.SUMMARY)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.DESCRIPTION)) {
            workItem.setDescription(WorkItemFieldValueUtils.asNullableString(data.getSystemField(WorkItemFieldConstants.DESCRIPTION)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.PRIORITY_ID)) {
            workItem.setPriorityId(resolvedPriorityId);
        }
        if (data.hasSystemField(WorkItemFieldConstants.ASSIGNEE_ID)) {
            workItem.setAssigneeId(resolvedAssigneeId);
        }
        if (data.hasSystemField(WorkItemFieldConstants.START_DATE)) {
            workItem.setStartDate(WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.START_DATE)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.DUE_DATE)) {
            workItem.setDueDate(WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE)) {
            workItem.setTimeOriginalEstimate(WorkItemFieldValueUtils.asNullableNonNegativeLong(
                    data.getSystemField(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE)));
        }
        if (data.hasSystemField(WorkItemFieldConstants.SECURITY_LEVEL_ID)) {
            workItem.setSecurityLevelId(resolvedSecurityLevelId);
        }
    }

    private List<String> buildChangedFields(Map<String, Object> originalSnapshot,
                                            WorkItemEntity updatedWorkItem,
                                            List<String> changedCustomFields) {
        List<String> changedFields = new ArrayList<>();
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.SUMMARY, updatedWorkItem.getSummary());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.DESCRIPTION, updatedWorkItem.getDescription());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.PRIORITY_ID, updatedWorkItem.getPriorityId());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.ASSIGNEE_ID, updatedWorkItem.getAssigneeId());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.START_DATE, updatedWorkItem.getStartDate());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.DUE_DATE, updatedWorkItem.getDueDate());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, updatedWorkItem.getTimeOriginalEstimate());
        addIfChanged(changedFields, originalSnapshot, WorkItemFieldConstants.SECURITY_LEVEL_ID, updatedWorkItem.getSecurityLevelId());

        if (changedCustomFields != null && !changedCustomFields.isEmpty()) {
            changedFields.addAll(changedCustomFields);
        }
        return changedFields;
    }

    private Map<String, Boolean> toRequiredCustomFieldMap(WorkItemFieldRules fieldRules) {
        return fieldRules.customPolicies().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().required(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private void validateScheduleRange(WorkItemEntity workItem, UpdateWorkItemData data) {
        Long effectiveStartDate = data.hasSystemField(WorkItemFieldConstants.START_DATE)
                ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.START_DATE))
                : workItem.getStartDate();
        Long effectiveDueDate = data.hasSystemField(WorkItemFieldConstants.DUE_DATE)
                ? WorkItemFieldValueUtils.asNullableNonNegativeLong(data.getSystemField(WorkItemFieldConstants.DUE_DATE))
                : workItem.getDueDate();
        WorkItemScheduleValidator.validateRange(effectiveStartDate, effectiveDueDate);
    }

    private void addIfChanged(List<String> changedFields,
                              Map<String, Object> originalSnapshot,
                              String fieldRef,
                              Object newValue) {
        if (!Objects.equals(originalSnapshot.get(fieldRef), newValue)) {
            changedFields.add(fieldRef);
        }
    }

    private void persistUpdatedOutboxEvent(WorkItemEntity updatedWorkItem,
                                           List<String> changedFields,
                                           Long tenantId) {
        Long eventTime = updatedWorkItem.getUpdatedAt() == null ? System.currentTimeMillis() : updatedWorkItem.getUpdatedAt();
        WorkItemEventPayload payload = WorkItemEventPayload.builder()
                .workItemId(updatedWorkItem.getId())
                .workItemKey(updatedWorkItem.getKey())
                .projectId(updatedWorkItem.getProjectId())
                .issueTypeId(updatedWorkItem.getIssueTypeId())
                .statusId(updatedWorkItem.getStatusId())
                .assigneeId(updatedWorkItem.getAssigneeId())
                .changedFields(changedFields)
                .build();

        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.WorkItem.AGGREGATE)
                .aggregateId(updatedWorkItem.getId())
                .eventType(EventConstants.WorkItem.EventType.WORK_ITEM_UPDATED)
                .topic(EventConstants.WorkItem.TOPIC)
                .partitionKey(String.valueOf(updatedWorkItem.getProjectId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(eventTime)
                .updatedAt(eventTime)
                .build();
        outboxEventService.saveEvent(outboxEvent);
    }

}
