/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.workitem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.command.workitem.validator.CreateWorkItemValidator;
import serp.project.pmcore.domain.constant.EventConstants;
import serp.project.pmcore.domain.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.dto.workitem.create.CreateWorkItemData;
import serp.project.pmcore.domain.dto.workitem.create.CreateFieldRules;
import serp.project.pmcore.domain.dto.workitem.create.ResolvedCustomFields;
import serp.project.pmcore.domain.dto.workitem.create.ResolvedWorkItemCreateConfiguration;
import serp.project.pmcore.domain.entity.OutboxEventEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemCustomFieldValueEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemEntity;
import serp.project.pmcore.domain.enums.OutboxEventStatus;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.port.store.IWorkItemCustomFieldValuePort;
import serp.project.pmcore.domain.service.IOutboxEventService;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.IWorkItemService;
import serp.project.pmcore.domain.service.workitem.create.WorkItemCreateAuthorizationService;
import serp.project.pmcore.domain.service.workitem.create.WorkItemCreateConfigurationResolver;
import serp.project.pmcore.domain.service.workitem.create.WorkItemCustomFieldResolver;
import serp.project.pmcore.domain.service.workitem.create.WorkItemCreateRequiredFieldValidator;
import serp.project.pmcore.domain.service.workitem.create.WorkItemDraftFactory;
import serp.project.pmcore.domain.service.workitem.create.WorkItemFieldPolicyResolver;
import serp.project.pmcore.domain.service.workitem.create.WorkItemFieldWriteValidator;
import serp.project.pmcore.kernel.utils.JsonUtils;
import serp.project.pmcore.ui.rest.workitem.dto.response.WorkItemResponse;

import java.util.List;
import java.util.Set;

@Service
public class CreateWorkItemCommand {

    private static final Logger log = LoggerFactory.getLogger(CreateWorkItemCommand.class);

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

    public CreateWorkItemCommand(CreateWorkItemValidator createWorkItemValidator,
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

    @Transactional(rollbackFor = Exception.class)
    public WorkItemResponse execute(Long projectId,
                                    CreateWorkItemData createWorkItemData,
                                    Long tenantId,
                                    Long userId,
                                    Set<String> groupKeys) {
        createWorkItemValidator.validate(createWorkItemData);

        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        ensureProjectWritable(project);
        ProjectPermissionEvaluationContext actorContext = workItemCreateAuthorizationService.buildActorContext(userId, groupKeys);
        workItemCreateAuthorizationService.checkCreatePermissions(project, actorContext);

        ResolvedWorkItemCreateConfiguration resolvedConfiguration = workItemCreateConfigurationResolver
                .resolve(project, createWorkItemData, tenantId);
        CreateFieldRules createFieldRules = workItemFieldPolicyResolver.resolveCreateFieldRules(
                project,
                resolvedConfiguration.issueType().getId(),
                tenantId
        );
        workItemFieldWriteValidator.validateClientSuppliedWritableFields(createWorkItemData, createFieldRules);

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
                createFieldRules
        );
        workItemCreateRequiredFieldValidator.validate(
                createWorkItemData,
                resolvedConfiguration.priorityId(),
                assigneeId,
                securityLevelId,
                createFieldRules,
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

        return WorkItemResponse.from(savedWorkItem);
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
}
