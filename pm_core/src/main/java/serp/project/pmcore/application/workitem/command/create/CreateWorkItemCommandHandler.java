/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.application.workitem.command.create.internal.CreateWorkItemData;
import serp.project.pmcore.application.workitem.command.create.internal.ResolvedWorkItemCreateConfiguration;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateAuthorizationService;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateConfigurationResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemCreateRequiredFieldValidator;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemDraftFactory;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemFieldPolicyResolver;
import serp.project.pmcore.application.workitem.command.create.support.WorkItemFieldWriteValidator;
import serp.project.pmcore.domain.customfield.dto.ResolvedCustomFields;
import serp.project.pmcore.domain.customfield.service.impl.WorkItemCustomFieldResolver;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldRules;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Map;

@Service
public class CreateWorkItemCommandHandler
        implements ICommandHandler<CreateWorkItemCommand, CreateWorkItemResult> {

    private static final Logger log = LoggerFactory.getLogger(CreateWorkItemCommandHandler.class);

    private final CreateWorkItemValidator createWorkItemValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final WorkItemCreateConfigurationResolver workItemCreateConfigurationResolver;
    private final WorkItemCreateAuthorizationService workItemCreateAuthorizationService;
    private final WorkItemCustomFieldResolver workItemCustomFieldResolver;
    private final WorkItemCreateRequiredFieldValidator workItemCreateRequiredFieldValidator;
    private final WorkItemDraftFactory workItemDraftFactory;
    private final WorkItemFieldPolicyResolver workItemFieldPolicyResolver;
    private final WorkItemFieldWriteValidator workItemFieldWriteValidator;
    private final IWorkItemCustomFieldValuePort workItemCustomFieldValuePort;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    public CreateWorkItemCommandHandler(CreateWorkItemValidator createWorkItemValidator,
                                        IProjectService projectService,
                                        IWorkItemService workItemService,
                                        WorkItemCreateConfigurationResolver workItemCreateConfigurationResolver,
                                        WorkItemCreateAuthorizationService workItemCreateAuthorizationService,
                                        WorkItemCustomFieldResolver workItemCustomFieldResolver,
                                        WorkItemCreateRequiredFieldValidator workItemCreateRequiredFieldValidator,
                                        WorkItemDraftFactory workItemDraftFactory,
                                        WorkItemFieldPolicyResolver workItemFieldPolicyResolver,
                                        WorkItemFieldWriteValidator workItemFieldWriteValidator,
                                        IWorkItemCustomFieldValuePort workItemCustomFieldValuePort,
                                        IOutboxEventService outboxEventService,
                                        JsonUtils jsonUtils) {
        this.createWorkItemValidator = createWorkItemValidator;
        this.projectService = projectService;
        this.workItemService = workItemService;
        this.workItemCreateConfigurationResolver = workItemCreateConfigurationResolver;
        this.workItemCreateAuthorizationService = workItemCreateAuthorizationService;
        this.workItemCustomFieldResolver = workItemCustomFieldResolver;
        this.workItemCreateRequiredFieldValidator = workItemCreateRequiredFieldValidator;
        this.workItemDraftFactory = workItemDraftFactory;
        this.workItemFieldPolicyResolver = workItemFieldPolicyResolver;
        this.workItemFieldWriteValidator = workItemFieldWriteValidator;
        this.workItemCustomFieldValuePort = workItemCustomFieldValuePort;
        this.outboxEventService = outboxEventService;
        this.jsonUtils = jsonUtils;
    }

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
        ProjectPermissionEvaluationContext actorContext = workItemCreateAuthorizationService
                .buildActorContext(userId, command.groupKeys());
        workItemCreateAuthorizationService.checkCreatePermissions(project, actorContext);

        ResolvedWorkItemCreateConfiguration resolvedConfiguration = workItemCreateConfigurationResolver
                .resolve(project, createWorkItemData, tenantId);
        WorkItemFieldRules fieldRules = workItemFieldPolicyResolver.resolveCreateFieldRules(
                project,
                resolvedConfiguration.issueType().getId(),
                tenantId
        );
        workItemFieldWriteValidator.validateClientSuppliedWritableFields(createWorkItemData, fieldRules);

        workItemCreateAuthorizationService.checkScheduleIssuesPermissionIfNeeded(project, actorContext, createWorkItemData.getDueDate());
        Long assigneeId = workItemCreateAuthorizationService.resolveAssigneeId(project, createWorkItemData.getAssigneeId(), actorContext);
        workItemCreateAuthorizationService.checkSetIssueSecurityPermissionIfNeeded(project, actorContext, createWorkItemData.getSecurityLevelId());
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

        ResolvedCustomFields resolvedCustomFields = workItemCustomFieldResolver.resolveCustomFields(
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
                resolvedCustomFields
        );

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
        persistCustomFieldValues(savedWorkItem.getId(), resolvedCustomFields.values(), tenantId, userId);
        persistCreatedOutboxEvent(savedWorkItem, tenantId, projectId);

        log.info("Created work item id={} key={} projectId={} tenantId={}",
                savedWorkItem.getId(), savedWorkItem.getKey(), projectId, tenantId);

        return CreateWorkItemResult.from(savedWorkItem);
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private void persistCustomFieldValues(Long workItemId,
                                          List<WorkItemCustomFieldValueEntity> values,
                                          Long tenantId,
                                          Long userId) {
        if (values == null || values.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        for (WorkItemCustomFieldValueEntity value : values) {
            value.setWorkItemId(workItemId);
            value.setTenantId(tenantId);
            value.applyCreate(userId, now);
        }

        workItemCustomFieldValuePort.saveAll(values);
    }

    private void persistCreatedOutboxEvent(WorkItemEntity workItem,
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

        outboxEventService.saveEvent(outboxEvent);
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
