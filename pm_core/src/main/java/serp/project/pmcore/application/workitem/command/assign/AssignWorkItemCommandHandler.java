/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.assign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.command.roleactor.RoleActorSubjectValidator;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.notification.service.IWorkItemNotificationOutboxPublisher;
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
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignWorkItemCommandHandler
        implements ICommandHandler<AssignWorkItemCommand, AssignWorkItemResult> {

    private final AssignWorkItemValidator assignWorkItemValidator;
    private final IProjectService projectService;
    private final IWorkItemService workItemService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;
    private final RoleActorSubjectValidator roleActorSubjectValidator;
    private final IOutboxEventService outboxEventService;
    private final JsonUtils jsonUtils;
    private final IWorkItemNotificationOutboxPublisher notificationOutboxPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssignWorkItemResult handle(AssignWorkItemCommand command) {
        assignWorkItemValidator.validate(command);

        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        ensureProjectWritable(project);
        ProjectPermissionSubject permissionSubject = ProjectPermissionSubject.from(project);

        WorkItemEntity workItem = workItemService.getWorkItemById(command.workItemId(), command.tenantId());
        ensureWorkItemBelongsToProject(workItem, project);

        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                command.userId(),
                command.groupKeys(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
        workItemAuthorizationSupportService.checkRequiredPermissions(
                permissionSubject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS,
                ProjectPermissionKeys.ASSIGN_ISSUES
        );
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);

        if (command.assigneeId() != null) {
            roleActorSubjectValidator.validateSubjectExistsForAdd(
                    ProjectRoleActorSubjectType.USER,
                    String.valueOf(command.assigneeId())
            );
        }

        Long previousAssigneeId = workItem.getAssigneeId();
        Long resolvedAssigneeId = workItemAuthorizationSupportService.resolveAssigneeId(
                permissionSubject,
                command.assigneeId(),
                actorContext
        );
        if (Objects.equals(previousAssigneeId, resolvedAssigneeId)) {
            log.info("Assignment unchanged for work item id={} projectId={} assigneeId={}",
                    workItem.getId(), project.getId(), resolvedAssigneeId);
            return AssignWorkItemResult.from(workItem);
        }

        workItem.setAssigneeId(resolvedAssigneeId);
        WorkItemEntity updatedWorkItem = workItemService.updateWorkItem(workItem, command.userId());
        OutboxEventEntity sourceEvent = persistAssignedOutboxEvent(
                updatedWorkItem,
                previousAssigneeId,
                command.tenantId(),
                command.userId()
        );
        notificationOutboxPublisher.publishWorkItemAssignedNotifications(
                project,
                updatedWorkItem,
                command.tenantId(),
                command.userId(),
                sourceEvent == null ? null : sourceEvent.getId()
        );

        log.info("Assigned work item id={} projectId={} previousAssigneeId={} assigneeId={}",
                updatedWorkItem.getId(),
                updatedWorkItem.getProjectId(),
                previousAssigneeId,
                updatedWorkItem.getAssigneeId());

        return AssignWorkItemResult.from(updatedWorkItem);
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

    private OutboxEventEntity persistAssignedOutboxEvent(WorkItemEntity workItem,
                                                         Long previousAssigneeId,
                                                         Long tenantId,
                                                         Long userId) {
        WorkItemEventPayload payload = WorkItemEventPayload.builder()
                .workItemId(workItem.getId())
                .workItemKey(workItem.getKey())
                .projectId(workItem.getProjectId())
                .issueTypeId(workItem.getIssueTypeId())
                .statusId(workItem.getStatusId())
                .assigneeId(workItem.getAssigneeId())
                .previousAssigneeId(previousAssigneeId)
                .assignedBy(userId)
                .assignedAt(workItem.getUpdatedAt())
                .changedFields(List.of(WorkItemFieldConstants.ASSIGNEE_ID))
                .build();

        OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .tenantId(tenantId)
                .aggregateType(EventConstants.WorkItem.AGGREGATE)
                .aggregateId(workItem.getId())
                .eventType(EventConstants.WorkItem.EventType.WORK_ITEM_ASSIGNED)
                .topic(EventConstants.WorkItem.TOPIC)
                .partitionKey(String.valueOf(workItem.getProjectId()))
                .payload(jsonUtils.toJson(payload))
                .status(OutboxEventStatus.PENDING)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        return outboxEventService.saveEvent(outboxEvent);
    }
}
