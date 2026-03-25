/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.command.workitem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.command.workitem.validator.CreateWorkItemValidator;
import serp.project.pmcore.domain.constant.EventConstants;
import serp.project.pmcore.domain.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.dto.message.WorkItemEventPayload;
import serp.project.pmcore.domain.dto.project.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.domain.dto.response.workitem.WorkItemResponse;
import serp.project.pmcore.domain.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.entity.IssueTypeSchemeItemEntity;
import serp.project.pmcore.domain.entity.OutboxEventEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.entity.PrioritySchemeItemEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeEntity;
import serp.project.pmcore.domain.entity.WorkflowSchemeItemEntity;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowStepEntity;
import serp.project.pmcore.domain.entity.workflow.WorkflowVersionEntity;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.entity.workitem.WorkItemEntity;
import serp.project.pmcore.domain.enums.OutboxEventStatus;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.DomainValidationException;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.port.store.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.port.store.IIssueTypePort;
import serp.project.pmcore.domain.port.store.IIssueTypeSchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemeItemPort;
import serp.project.pmcore.domain.port.store.IPrioritySchemePort;
import serp.project.pmcore.domain.port.store.IWorkflowPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemeItemPort;
import serp.project.pmcore.domain.port.store.IWorkflowSchemePort;
import serp.project.pmcore.domain.port.store.IWorkflowStepPort;
import serp.project.pmcore.domain.port.store.IWorkflowVersionPort;
import serp.project.pmcore.domain.service.IOutboxEventService;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateWorkItemCommand {

    private final CreateWorkItemValidator createWorkItemValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IIssueTypePort issueTypePort;
    private final IIssueTypeSchemeItemPort issueTypeSchemeItemPort;
    private final IWorkflowSchemePort workflowSchemePort;
    private final IWorkflowSchemeItemPort workflowSchemeItemPort;
    private final IWorkflowPort workflowPort;
    private final IWorkflowVersionPort workflowVersionPort;
    private final IWorkflowStepPort workflowStepPort;
    private final IPrioritySchemePort prioritySchemePort;
    private final IPrioritySchemeItemPort prioritySchemeItemPort;
    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;

    @Transactional(rollbackFor = Exception.class)
    public WorkItemResponse execute(Long projectId,
                                    CreateWorkItemRequest request,
                                    Long tenantId,
                                    Long userId,
                                    Set<String> groupKeys) {
        createWorkItemValidator.validate(request);

        ProjectEntity project = projectService.getProjectById(projectId, tenantId);
        ensureProjectWritable(project);

        ProjectPermissionEvaluationContext actorContext = ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .build();

        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS);
        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.CREATE_ISSUES);

        IssueTypeEntity issueType = issueTypePort.getIssueTypeById(request.getIssueTypeId(), tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(request.getIssueTypeId()));
        validateIssueTypeInProjectScheme(project, issueType.getId(), tenantId);
        validateParentRequirement(issueType, request.getParentId());

        WorkflowStepEntity initialStep = resolveInitialWorkflowStep(project, issueType.getId(), tenantId);
        Long priorityId = resolvePriorityId(project, request.getPriorityId(), tenantId);

        if (request.getDueDate() != null) {
            projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.SCHEDULE_ISSUES);
        }

        Long assigneeId = resolveAssigneeId(project, request.getAssigneeId(), actorContext, tenantId);
        Long securityLevelId = resolveSecurityLevelId(project, request.getSecurityLevelId(), actorContext, tenantId);

        if (request.getParentId() != null) {
            workItemService.validateParentHierarchy(request.getParentId(), issueType.getId(), projectId, tenantId);
        }

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
                .workflowStepId(initialStep.getId())
                .statusId(initialStep.getStatusId())
                .priorityId(priorityId)
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
        persistCreatedOutboxEvent(savedWorkItem, tenantId, userId, projectId);

        log.info("Created work item id={} key={} projectId={} tenantId={}",
                savedWorkItem.getId(), savedWorkItem.getKey(), projectId, tenantId);

        return WorkItemResponse.from(savedWorkItem);
    }

    private void ensureProjectWritable(ProjectEntity project) {
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }
    }

    private void validateIssueTypeInProjectScheme(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getIssueTypeSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_TYPE_SCHEME_NOT_FOUND,
                    "Project has no issue type scheme binding: projectId=" + project.getId()
            );
        }

        boolean exists = issueTypeSchemeItemPort.getIssueTypeSchemeItemsBySchemeId(
                        project.getIssueTypeSchemeId(),
                        tenantId
                )
                .stream()
                .map(IssueTypeSchemeItemEntity::getIssueTypeId)
                .anyMatch(issueTypeId::equals);

        if (!exists) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.ISSUE_TYPE_NOT_IN_SCHEME,
                    "Issue type is not allowed in project scheme: projectId=" + project.getId() + ", issueTypeId=" + issueTypeId
            );
        }
    }

    private void validateParentRequirement(IssueTypeEntity issueType, Long parentId) {
        if (issueType.getHierarchyLevel() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Issue type hierarchy level is missing: issueTypeId=" + issueType.getId()
            );
        }

        if (issueType.getHierarchyLevel() == 0 && parentId == null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Subtask issue type requires parent_id: issueTypeId=" + issueType.getId()
            );
        }

        if (issueType.getHierarchyLevel() >= 2 && parentId != null) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.INVALID_PARENT_HIERARCHY,
                    "Issue types with hierarchy level >= 2 cannot set parent_id: issueTypeId=" + issueType.getId()
                            + ", hierarchyLevel=" + issueType.getHierarchyLevel()
            );
        }
    }

    private WorkflowStepEntity resolveInitialWorkflowStep(ProjectEntity project, Long issueTypeId, Long tenantId) {
        if (project.getWorkflowSchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                    "Project has no workflow scheme binding: projectId=" + project.getId()
            );
        }

        WorkflowSchemeEntity workflowScheme = workflowSchemePort
                .getWorkflowSchemeById(project.getWorkflowSchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_SCHEME_NOT_FOUND,
                        "Workflow scheme not found: id=" + project.getWorkflowSchemeId()
                ));

        Long workflowId = workflowSchemeItemPort.getWorkflowSchemeItemsBySchemeId(
                        project.getWorkflowSchemeId(),
                        tenantId
                )
                .stream()
                .filter(item -> issueTypeId.equals(item.getIssueTypeId()))
                .map(WorkflowSchemeItemEntity::getWorkflowId)
                .findFirst()
                .orElse(workflowScheme.getDefaultWorkflowId());

        if (workflowId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_SCHEME_COVERAGE_MISSING,
                    "Workflow scheme does not cover issueTypeId=" + issueTypeId + " for projectId=" + project.getId()
            );
        }

        WorkflowEntity workflow = workflowPort.getWorkflowById(workflowId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workflow(workflowId));

        if (workflow.getCurrentPublishedVersionId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow has no published version: workflowId=" + workflowId
            );
        }

        WorkflowVersionEntity publishedVersion = workflowVersionPort
                .getWorkflowVersionById(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                        "Published workflow version not found: id=" + workflow.getCurrentPublishedVersionId()
                ));

        if (!publishedVersion.isActive()) {
            throw new DomainValidationException(
                    DomainErrorCode.WORKFLOW_NOT_ACTIVE,
                    "Workflow current version is not published: workflowId=" + workflowId
                            + ", versionId=" + publishedVersion.getId()
            );
        }

        return workflowStepPort.getInitialStepByWorkflowVersionId(workflow.getCurrentPublishedVersionId(), tenantId)
                .orElseThrow(() -> new DomainValidationException(
                        DomainErrorCode.WORKFLOW_NO_INITIAL_STEP,
                        "Workflow must have exactly one initial step: workflowId=" + workflowId
                ));
    }

    private Long resolvePriorityId(ProjectEntity project, Long requestedPriorityId, Long tenantId) {
        if (project.getPrioritySchemeId() == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                    "Project has no priority scheme binding: projectId=" + project.getId()
            );
        }

        PrioritySchemeEntity priorityScheme = prioritySchemePort
                .getPrioritySchemeById(project.getPrioritySchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PRIORITY_SCHEME_NOT_FOUND,
                        "Priority scheme not found: id=" + project.getPrioritySchemeId()
                ));

        List<PrioritySchemeItemEntity> priorityItems = prioritySchemeItemPort
                .getPrioritySchemeItemsBySchemeId(project.getPrioritySchemeId(), tenantId);

        if (requestedPriorityId != null) {
            boolean inScheme = priorityItems.stream()
                    .map(PrioritySchemeItemEntity::getPriorityId)
                    .anyMatch(requestedPriorityId::equals);
            if (!inScheme) {
                throw new BusinessRuleViolationException(
                        DomainErrorCode.PRIORITY_NOT_IN_SCHEME,
                        "Priority is not allowed in project scheme: projectId=" + project.getId() + ", priorityId=" + requestedPriorityId
                );
            }
            return requestedPriorityId;
        }

        if (priorityScheme.getDefaultPriorityId() == null) {
            throw new DomainValidationException(
                    DomainErrorCode.DEFAULT_PRIORITY_NOT_CONFIGURED,
                    "Priority scheme has no default priority: schemeId=" + project.getPrioritySchemeId()
            );
        }

        return priorityScheme.getDefaultPriorityId();
    }

    private Long resolveAssigneeId(ProjectEntity project,
                                   Long requestedAssigneeId,
                                   ProjectPermissionEvaluationContext actorContext,
                                   Long tenantId) {
        if (requestedAssigneeId == null) {
            return null;
        }

        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.ASSIGN_ISSUES);

        ProjectPermissionEvaluationContext assigneeContext = ProjectPermissionEvaluationContext.builder()
                .userId(requestedAssigneeId)
                .build();

        if (!projectPermissionEvaluationService.hasPermission(project, assigneeContext, ProjectPermissionKeys.ASSIGNABLE_USER)) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.PROJECT_PERMISSION_DENIED,
                    "Assignee is not assignable in project: projectId=" + project.getId() + ", assigneeId=" + requestedAssigneeId
            );
        }

        return requestedAssigneeId;
    }

    private Long resolveSecurityLevelId(ProjectEntity project,
                                        Long requestedSecurityLevelId,
                                        ProjectPermissionEvaluationContext actorContext,
                                        Long tenantId) {
        if (project.getIssueSecuritySchemeId() == null) {
            return requestedSecurityLevelId == null ? null : missingIssueSecurityScheme(project.getId());
        }

        IssueSecuritySchemeEntity issueSecurityScheme = issueSecuritySchemePort
                .getIssueSecuritySchemeById(project.getIssueSecuritySchemeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                        "Issue security scheme not found: id=" + project.getIssueSecuritySchemeId()
                ));

        if (requestedSecurityLevelId == null) {
            return issueSecurityScheme.getDefaultLevelId();
        }

        projectPermissionEvaluationService.checkPermission(project, actorContext, ProjectPermissionKeys.SET_ISSUE_SECURITY);

        boolean inScheme = issueSecurityLevelPort.getIssueSecurityLevelsBySchemeId(
                        project.getIssueSecuritySchemeId(),
                        tenantId
                )
                .stream()
                .map(IssueSecurityLevelEntity::getId)
                .anyMatch(requestedSecurityLevelId::equals);

        if (!inScheme) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.SECURITY_LEVEL_NOT_IN_SCHEME,
                    "Security level is not allowed in project scheme: projectId=" + project.getId()
                            + ", securityLevelId=" + requestedSecurityLevelId
            );
        }

        return requestedSecurityLevelId;
    }

    private Long missingIssueSecurityScheme(Long projectId) {
        throw new ResourceNotFoundException(
                DomainErrorCode.ISSUE_SECURITY_SCHEME_NOT_FOUND,
                "Project has no issue security scheme binding: projectId=" + projectId
        );
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
