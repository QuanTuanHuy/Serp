/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.workitem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.command.workitem.validator.CreateWorkItemValidator;
import serp.project.pmcore.domain.constant.EventConstants;
import serp.project.pmcore.domain.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.domain.dto.response.workitem.WorkItemResponse;
import serp.project.pmcore.domain.dto.workitem.create.CreateFieldRules;
import serp.project.pmcore.domain.dto.workitem.create.FieldPolicy;
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
import serp.project.pmcore.domain.service.workitem.create.WorkItemFieldPolicyResolver;
import serp.project.pmcore.domain.service.workitem.create.WorkItemFieldWriteValidator;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class CreateWorkItemCommand {

    private final CreateWorkItemValidator createWorkItemValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final WorkItemCreateConfigurationResolver workItemCreateConfigurationResolver;
    private final WorkItemCreateAuthorizationService workItemCreateAuthorizationService;
    private final WorkItemCustomFieldResolver workItemCustomFieldResolver;
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
        this.workItemFieldPolicyResolver = workItemFieldPolicyResolver;
        this.workItemFieldWriteValidator = workItemFieldWriteValidator;
        this.workItemCustomFieldValuePort = workItemCustomFieldValuePort;
        this.outboxEventService = outboxEventService;
        this.jsonUtils = jsonUtils;
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkItemResponse execute(Long projectId,
                                    CreateWorkItemRequest request,
                                    Long tenantId,
                                    Long userId,
                                    Set<String> groupKeys) {
        createWorkItemValidator.validate(request);

        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        ProjectPermissionEvaluationContext actorContext = workItemCreateAuthorizationService.buildActorContext(userId, groupKeys);
        workItemCreateAuthorizationService.checkCreatePermissions(project, actorContext);

        ResolvedWorkItemCreateConfiguration resolvedConfiguration = workItemCreateConfigurationResolver
                .resolve(project, request, tenantId);
        CreateFieldRules createFieldRules = workItemFieldPolicyResolver.resolveCreateFieldRules(
                project,
                resolvedConfiguration.issueType().getId(),
                tenantId
        );
        workItemFieldWriteValidator.validateClientSuppliedWritableFields(request, createFieldRules);

        workItemCreateAuthorizationService.checkScheduleIssuesPermissionIfNeeded(project, actorContext, request.getDueDate());
        Long assigneeId = workItemCreateAuthorizationService.resolveAssigneeId(project, request.getAssigneeId(), actorContext);
        workItemCreateAuthorizationService.checkSetIssueSecurityPermissionIfNeeded(project, actorContext, request.getSecurityLevelId());
        Long securityLevelId = workItemCreateConfigurationResolver.resolveSecurityLevelId(
                project,
                request.getSecurityLevelId(),
                tenantId
        );

        if (request.getParentId() != null) {
            workItemService.validateParentHierarchy(
                    request.getParentId(),
                    resolvedConfiguration.issueType().getId(),
                    projectId,
                    tenantId
            );
        }

        ResolvedCustomFields resolvedCustomFields = workItemCustomFieldResolver.resolveCustomFields(
                projectId,
                resolvedConfiguration.issueType().getId(),
                request.getCustomFields(),
                createFieldRules,
                tenantId
        );
        validateRequiredFields(
                request,
                resolvedConfiguration.priorityId(),
                assigneeId,
                securityLevelId,
                createFieldRules,
                resolvedCustomFields
        );

        long issueNo = workItemService.getNextIssueNumber(projectId, tenantId);
        String key = project.getKey() + "-" + issueNo;
        String rank = workItemService.getNextRank(projectId, tenantId);

        WorkItemEntity workItem = WorkItemEntity.builder()
                .projectId(projectId)
                .issueTypeId(request.getIssueTypeId())
                .issueNo(issueNo)
                .key(key)
                .summary(request.getSummary())
                .description(request.getDescription())
                .workflowStepId(resolvedConfiguration.initialStep().getId())
                .statusId(resolvedConfiguration.initialStep().getStatusId())
                .priorityId(resolvedConfiguration.priorityId())
                .resolutionId(null)
                .assigneeId(assigneeId)
                .reporterId(userId)
                .parentId(request.getParentId())
                .securityLevelId(securityLevelId)
                .dueDate(request.getDueDate())
                .rank(rank)
                .timeOriginalEstimate(request.getTimeOriginalEstimate())
                .timeRemainingEstimate(request.getTimeOriginalEstimate())
                .timeSpent(0L)
                .build();

        WorkItemEntity savedWorkItem = workItemService.createWorkItem(workItem, tenantId, userId);
        persistCustomFieldValues(savedWorkItem.getId(), resolvedCustomFields.values(), tenantId, userId);
        persistCreatedOutboxEvent(savedWorkItem, tenantId, userId, projectId);

        log.info("Created work item id={} key={} projectId={} tenantId={}",
                savedWorkItem.getId(), savedWorkItem.getKey(), projectId, tenantId);

        return WorkItemResponse.from(savedWorkItem);
    }

    private void validateRequiredFields(CreateWorkItemRequest request,
                                        Long priorityId,
                                        Long assigneeId,
                                        Long securityLevelId,
                                        CreateFieldRules createFieldRules,
                                        ResolvedCustomFields resolvedCustomFields) {
        List<String> missingFields = new ArrayList<>();

        Map<String, Object> effectiveSystemValues = new LinkedHashMap<>();
        effectiveSystemValues.put(WorkItemFieldConstants.ISSUE_TYPE_ID, request.getIssueTypeId());
        effectiveSystemValues.put(WorkItemFieldConstants.SUMMARY, request.getSummary());
        effectiveSystemValues.put(WorkItemFieldConstants.DESCRIPTION, request.getDescription());
        effectiveSystemValues.put(WorkItemFieldConstants.PRIORITY_ID, priorityId);
        effectiveSystemValues.put(WorkItemFieldConstants.ASSIGNEE_ID, assigneeId);
        effectiveSystemValues.put(WorkItemFieldConstants.PARENT_ID, request.getParentId());
        effectiveSystemValues.put(WorkItemFieldConstants.DUE_DATE, request.getDueDate());
        effectiveSystemValues.put(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, request.getTimeOriginalEstimate());
        effectiveSystemValues.put(WorkItemFieldConstants.SECURITY_LEVEL_ID, securityLevelId);

        for (FieldPolicy systemPolicy : createFieldRules.systemPolicies().values()) {
            if (!systemPolicy.required() || !WorkItemFieldConstants.SUPPORTED_CREATE_SYSTEM_FIELDS.contains(systemPolicy.fieldRef())) {
                continue;
            }
            if (isMissingValue(effectiveSystemValues.get(systemPolicy.fieldRef()))) {
                missingFields.add(systemPolicy.fieldRef());
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
            value.setCreatedAt(now);
            value.setCreatedBy(userId);
            value.setUpdatedAt(now);
            value.setUpdatedBy(userId);
        }

        workItemCustomFieldValuePort.saveAll(values);
    }

    private void persistCreatedOutboxEvent(WorkItemEntity workItem,
                                           Long tenantId,
                                           Long userId,
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
