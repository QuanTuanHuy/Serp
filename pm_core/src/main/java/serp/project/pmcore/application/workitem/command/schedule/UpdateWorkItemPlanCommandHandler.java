/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanAllocationPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UpdateWorkItemPlanCommandHandler
        implements ICommandHandler<UpdateWorkItemPlanCommand, UpdateWorkItemPlanResult> {

    private final IProjectService projectService;
    private final IWorkItemReadPort workItemReadPort;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;
    private final IWorkItemPlanPort workItemPlanPort;
    private final IWorkItemPlanAllocationPort workItemPlanAllocationPort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateWorkItemPlanResult handle(UpdateWorkItemPlanCommand command) {
        validate(command);

        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }

        WorkItemEntity workItem = workItemReadPort.getWorkItemById(command.workItemId(), command.tenantId())
                .orElseThrow(() -> ResourceNotFoundException.workItem(command.workItemId()));
        if (!Objects.equals(workItem.getProjectId(), command.projectId())) {
            throw ResourceNotFoundException.workItem(command.workItemId());
        }

        ProjectPermissionSubject permissionSubject = ProjectPermissionSubject.from(project);
        ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                command.userId(),
                command.groupKeys(),
                workItem.getReporterId(),
                workItem.getAssigneeId()
        );
        workItemAuthorizationSupportService.checkRequiredPermissions(
                permissionSubject,
                actorContext,
                ProjectPermissionKeys.BROWSE_PROJECTS
        );
        workItemAuthorizationSupportService.checkScheduleIssuesPermission(permissionSubject, actorContext);
        issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);

        long now = System.currentTimeMillis();
        WorkItemPlanEntity plan = WorkItemPlanEntity.builder()
                .tenantId(command.tenantId())
                .projectId(command.projectId())
                .workItemId(command.workItemId())
                .plannedStart(command.plannedStart())
                .plannedEnd(command.plannedEnd())
                .source(WorkItemPlanSource.MANUAL)
                .sourceRunId(null)
                .locked(command.locked() == null ? Boolean.TRUE : command.locked())
                .build();
        plan.applyCreate(command.userId(), now);

        WorkItemPlanEntity savedPlan = workItemPlanPort.upsertActivePlan(plan);
        List<WorkItemPlanAllocationEntity> savedAllocations = savedPlan.getId() == null
                ? List.of()
                : workItemPlanAllocationPort.replaceForPlan(
                        command.tenantId(),
                        savedPlan.getId(),
                        toAllocations(command, savedPlan, now)
                );
        return UpdateWorkItemPlanResult.from(savedPlan, savedAllocations);
    }

    private List<WorkItemPlanAllocationEntity> toAllocations(UpdateWorkItemPlanCommand command,
                                                             WorkItemPlanEntity savedPlan,
                                                             long now) {
        return command.allocations().stream()
                .map(allocation -> toAllocation(command, savedPlan, allocation, now))
                .toList();
    }

    private WorkItemPlanAllocationEntity toAllocation(UpdateWorkItemPlanCommand command,
                                                      WorkItemPlanEntity savedPlan,
                                                      UpdateWorkItemPlanAllocationCommand allocation,
                                                      long now) {
        WorkItemPlanAllocationEntity entity = WorkItemPlanAllocationEntity.builder()
                .tenantId(command.tenantId())
                .projectId(command.projectId())
                .workItemPlanId(savedPlan.getId())
                .workItemId(command.workItemId())
                .assigneeId(allocation.assigneeId())
                .startTime(allocation.start())
                .endTime(allocation.end())
                .effortMillis(allocation.effortMillis())
                .source(WorkItemPlanSource.MANUAL)
                .sourceRunId(null)
                .sourceRunItemId(null)
                .build();
        entity.applyCreate(command.userId(), now);
        return entity;
    }

    private void validate(UpdateWorkItemPlanCommand command) {
        if (command == null) {
            throw invalidSchedule("command is required");
        }
        if (!isPositive(command.tenantId())
                || !isPositive(command.userId())
                || !isPositive(command.projectId())
                || !isPositive(command.workItemId())) {
            throw invalidSchedule("tenantId, userId, projectId, and workItemId must be positive");
        }
        if (!isPositive(command.plannedStart()) || !isPositive(command.plannedEnd())) {
            throw invalidSchedule("plannedStart and plannedEnd must be positive");
        }
        if (command.plannedStart() >= command.plannedEnd()) {
            throw invalidSchedule("plannedStart must be before plannedEnd");
        }
        for (UpdateWorkItemPlanAllocationCommand allocation : command.allocations()) {
            validateAllocation(command, allocation);
        }
    }

    private void validateAllocation(UpdateWorkItemPlanCommand command,
                                    UpdateWorkItemPlanAllocationCommand allocation) {
        if (allocation == null) {
            throw invalidSchedule("allocation is required");
        }
        if (!isPositive(allocation.assigneeId())
                || !isPositive(allocation.start())
                || !isPositive(allocation.end())
                || !isPositive(allocation.effortMillis())) {
            throw invalidSchedule("allocation fields must be positive");
        }
        if (allocation.start() >= allocation.end()) {
            throw invalidSchedule("allocation start must be before end");
        }
        if (allocation.start() < command.plannedStart() || allocation.end() > command.plannedEnd()) {
            throw invalidSchedule("allocation must be inside plan range");
        }
    }

    private boolean isPositive(Long value) {
        return value != null && value > 0;
    }

    private BusinessRuleViolationException invalidSchedule(String detail) {
        return new BusinessRuleViolationException(DomainErrorCode.WORK_ITEM_SCHEDULE_INVALID, detail);
    }
}
