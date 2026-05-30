/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.command.create.internal.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.internal.ResolvedWorkItemCreateConfiguration;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateConfigurationResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateRequiredFieldValidator;
import serp.project.pmcore.application.workitem.command.create.support.CreateWorkItemFieldRulesResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemDraftFactory;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemFieldWriteValidator;
import serp.project.pmcore.domain.customfield.dto.WorkItemCustomFieldMutationPlan;
import serp.project.pmcore.domain.notification.service.IWorkItemNotificationOutboxPublisher;
import serp.project.pmcore.domain.customfield.service.IWorkItemCustomFieldMutationService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.domain.workitem.validator.WorkItemScheduleValidator;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateWorkItemCommandHandler
        implements ICommandHandler<CreateWorkItemCommand, CreateWorkItemResult> {

    private final CreateWorkItemValidator createWorkItemValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final WorkItemCreateConfigurationResolver workItemCreateConfigurationResolver;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IWorkItemCustomFieldMutationService workItemCustomFieldMutationService;
    private final WorkItemCreateRequiredFieldValidator workItemCreateRequiredFieldValidator;
    private final WorkItemDraftFactory workItemDraftFactory;
    private final CreateWorkItemFieldRulesResolver createWorkItemFieldRulesResolver;
    private final WorkItemFieldWriteValidator workItemFieldWriteValidator;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;
    private final IWorkItemNotificationOutboxPublisher notificationOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateWorkItemResult handle(CreateWorkItemCommand command) {
        createWorkItemValidator.validate(command);

        Long projectId = command.projectId();
        Long tenantId = command.tenantId();
        Long userId = command.userId();
        CreateWorkItemData createWorkItemData = command.toCreateWorkItemData();

        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        ensureProjectWritable(project);
        ProjectPermissionSubject permissionSubject = ProjectPermissionSubject.from(project);
        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService
                .buildActorContext(userId, command.groupKeys());
        workItemAuthorizationSupportService.checkRequiredPermissions(
                permissionSubject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.CREATE_ISSUES
        );

        ResolvedWorkItemCreateConfiguration resolvedConfiguration = workItemCreateConfigurationResolver
                .resolve(project, createWorkItemData, tenantId);
        WorkItemFieldRules fieldRules = createWorkItemFieldRulesResolver.resolveCreateFieldRules(
                project,
                resolvedConfiguration.issueType().getId(),
                tenantId
        );
        workItemFieldWriteValidator.validateClientSuppliedWritableFields(createWorkItemData, fieldRules);

        if (createWorkItemData.getStartDate() != null || createWorkItemData.getDueDate() != null) {
            workItemAuthorizationSupportService.checkScheduleIssuesPermission(permissionSubject, actorContext);
        }
        Long assigneeId = workItemAuthorizationSupportService.resolveAssigneeId(permissionSubject, createWorkItemData.getAssigneeId(), actorContext);
        workItemAuthorizationSupportService.checkSetIssueSecurityPermissionIfNeeded(permissionSubject, actorContext, createWorkItemData.getSecurityLevelId());
        Long securityLevelId = workItemCreateConfigurationResolver.resolveSecurityLevelId(
                project,
                createWorkItemData.getSecurityLevelId(),
                tenantId
        );

        if (createWorkItemData.getParentId() != null) {
            workItemService.validateParentHierarchy(
                    createWorkItemData.getParentId(),
                    resolvedConfiguration.issueType().getId(),
                    projectId,
                    tenantId
            );
        }

        WorkItemCustomFieldMutationPlan customFieldPlan = workItemCustomFieldMutationService.planCreate(
                resolvedConfiguration.issueType().getTypeKey(),
                createWorkItemData.getCustomFields(),
                toRequiredCustomFieldMap(fieldRules)
        );
        workItemCreateRequiredFieldValidator.validate(
                createWorkItemData,
                resolvedConfiguration.priorityId(),
                assigneeId,
                securityLevelId,
                fieldRules,
                customFieldPlan.missingRequiredFields()
        );
        WorkItemScheduleValidator.validateRange(createWorkItemData.getStartDate(), createWorkItemData.getDueDate());

        long issueNo = workItemService.getNextIssueNumber(projectId, tenantId);
        String key = project.getKey() + "-" + issueNo;
        String rank = workItemService.getNextRank(projectId, tenantId);

        WorkItemEntity workItem = workItemDraftFactory.buildDraft(
                projectId,
                createWorkItemData,
                resolvedConfiguration,
                issueNo,
                key,
                rank,
                assigneeId,
                userId,
                securityLevelId
        );

        WorkItemEntity savedWorkItem = workItemService.createWorkItem(workItem, tenantId, userId);
        workItemCustomFieldMutationService.applyPlan(savedWorkItem.getId(), tenantId, userId, customFieldPlan);
        OutboxEventEntity sourceEvent = persistCreatedOutboxEvent(savedWorkItem, tenantId, projectId);
        notificationOutboxPublisher.publishWorkItemCreatedNotifications(
                project,
                savedWorkItem,
                tenantId,
                userId,
                sourceEvent == null ? null : sourceEvent.getId()
        );

        log.info("Created work item id={} key={} projectId={} tenantId={}",
                savedWorkItem.getId(), savedWorkItem.getKey(), projectId, tenantId);

        return CreateWorkItemResult.from(savedWorkItem);
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private OutboxEventEntity persistCreatedOutboxEvent(WorkItemEntity workItem,
                                                        Long tenantId,
                                                        Long projectId) {
        WorkItemEventPayload payload = WorkItemEventPayload.builder()
                .workItemId(workItem.getId())
                .workItemKey(workItem.getKey())
                .projectId(projectId)
                .issueTypeId(workItem.getIssueTypeId())
                .statusId(workItem.getStatusId())
                .assigneeId(workItem.getAssigneeId())
                .build();

        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.WorkItem.AGGREGATE)
                .aggregateId(workItem.getId())
                .eventType(EventConstants.WorkItem.EventType.WORK_ITEM_CREATED)
                .topic(EventConstants.WorkItem.TOPIC)
                .partitionKey(String.valueOf(projectId))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        return outboxEventService.saveEvent(outboxEvent);
    }

    private Map<String, Boolean> toRequiredCustomFieldMap(WorkItemFieldRules fieldRules) {
        return fieldRules.customPolicies().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().required(),
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ));
    }
}
