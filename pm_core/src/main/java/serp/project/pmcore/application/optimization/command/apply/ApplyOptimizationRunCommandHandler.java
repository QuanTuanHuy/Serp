/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuesecurity.dto.IssueSecurityAccessContext;
import serp.project.pmcore.domain.issuesecurity.service.IIssueSecurityService;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanAllocationEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.enums.WorkItemPlanSource;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleAllocation;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanAllocationPort;
import serp.project.pmcore.domain.optimization.port.IWorkItemPlanPort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.domain.workitem.service.IWorkItemAuthorizationSupportService;
import serp.project.pmcore.domain.workitem.service.IWorkItemService;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyOptimizationRunCommandHandler
        implements ICommandHandler<ApplyOptimizationRunCommand, OptimizationRunReviewView> {

    private static final String STALE_ITEM_REASON = "STALE_ITEM";
    private static final String LOCKED_PLAN_REASON = "LOCKED_PLAN";
    private static final String PERMISSION_DENIED_REASON = "PERMISSION_DENIED";
    private static final String INVALID_CHANGE_REASON = "INVALID_CHANGE";

    private final IOptimizationRunPort optimizationRunPort;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final IWorkItemPlanPort workItemPlanPort;
    private final IWorkItemPlanAllocationPort workItemPlanAllocationPort;
    private final IWorkItemReadPort workItemReadPort;
    private final IWorkItemService workItemService;
    private final IProjectService projectService;
    private final IWorkItemAuthorizationSupportService workItemAuthorizationSupportService;
    private final IIssueSecurityService issueSecurityService;
    private final OptimizationRunGuard optimizationRunGuard;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    private final JsonUtils jsonUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OptimizationRunReviewView handle(ApplyOptimizationRunCommand command) {
        validate(command);
        OptimizationRunEntity run = optimizationRunGuard.requireRunInProject(
                command.tenantId(),
                command.projectId(),
                command.runId()
        );
        optimizationRunGuard.ensureStatus(
                run,
                Set.of(OptimizationRunStatus.GENERATED, OptimizationRunStatus.PARTIALLY_APPLIED),
                "applied"
        );
        ProjectEntity project = projectService.getProjectById(command.projectId(), command.tenantId());
        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }

        // Apply trusts persisted run-item decisions, not suggestion details supplied by the request.
        List<OptimizationRunItemEntity> allItems = optimizationRunItemPort.listByRunId(command.tenantId(), command.runId());
        Map<Long, OptimizationRunItemEntity> itemsByWorkItemId = allItems.stream()
                .collect(Collectors.toMap(OptimizationRunItemEntity::getWorkItemId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        List<OptimizationRunItemEntity> selectedItems = command.workItemIds().stream()
                .map(workItemId -> requireRunItem(command, itemsByWorkItemId, workItemId))
                .sorted(Comparator.comparing(OptimizationRunItemEntity::getWorkItemId))
                .toList();

        Map<Long, WorkItemEntity> workItemsById = workItemReadPort
                .listActiveByWorkItemIds(command.tenantId(), command.workItemIds())
                .stream()
                .collect(Collectors.toMap(WorkItemEntity::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, WorkItemPlanEntity> activePlansByWorkItemId = workItemPlanPort
                .listActivePlansByWorkItemIds(command.tenantId(), command.workItemIds())
                .stream()
                .collect(Collectors.toMap(WorkItemPlanEntity::getWorkItemId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        ProjectPermissionSubject permissionSubject = ProjectPermissionSubject.from(project);
        List<OptimizationRunWarningEntity> warnings = new ArrayList<>();
        ApplyCounters counters = new ApplyCounters();
        long now = System.currentTimeMillis();

        for (OptimizationRunItemEntity item : selectedItems) {
            WorkItemEntity workItem = workItemsById.get(item.getWorkItemId());
            WorkItemPlanEntity activePlan = activePlansByWorkItemId.get(item.getWorkItemId());
            // A changed work item invalidates both assignment and schedule suggestions from the snapshot.
            boolean workItemStale = workItem == null || !Objects.equals(workItem.getUpdatedAt(), item.getWorkItemUpdatedAtSnapshot());
            if (Boolean.TRUE.equals(command.applyAssignment())) {
                applyAssignment(command, permissionSubject, project, item, workItem, warnings, counters, now);
            }
            if (Boolean.TRUE.equals(command.applySchedule())) {
                applySchedule(command, permissionSubject, project, item, workItem, activePlan, workItemStale, warnings, counters, now);
            }
            item.applyUpdate(command.userId(), now);
        }

        // No actionable accepted/overridden changes means the run stays GENERATED for further review.
        if (counters.actionCount > 0) {
            updateRunStatus(run, command.userId(), now, counters);
            optimizationRunPort.save(run);
            optimizationRunItemPort.saveAll(selectedItems);
            if (!warnings.isEmpty()) {
                optimizationRunWarningPort.saveAll(warnings);
            }
            log.info("Applied optimization run id={} tenantId={} projectId={} applied={} skipped={}",
                    run.getId(), run.getTenantId(), run.getProjectId(), counters.appliedCount, counters.skippedCount);
        }

        List<OptimizationRunItemEntity> savedItems = optimizationRunItemPort.listByRunId(command.tenantId(), command.runId());
        List<OptimizationRunWarningEntity> savedWarnings = optimizationRunWarningPort.listByRunId(command.tenantId(), command.runId());
        return optimizationRunReviewAssembler.toView(run, savedItems, savedWarnings);
    }

    private void applyAssignment(ApplyOptimizationRunCommand command,
                                 ProjectPermissionSubject permissionSubject,
                                 ProjectEntity project,
                                 OptimizationRunItemEntity item,
                                 WorkItemEntity workItem,
                                 List<OptimizationRunWarningEntity> warnings,
                                 ApplyCounters counters,
                                 long now) {
        if (!isActionable(item.getAssignmentDecision()) || item.getAssignmentApplyStatus() == OptimizationApplyStatus.APPLIED) {
            return;
        }
        counters.actionCount++;
        // Overrides are stored on the run item; apply never accepts final assignee details from the request body.
        Long assigneeId = item.getAssignmentDecision() == OptimizationDecision.OVERRIDDEN
                ? item.getOverrideAssigneeId()
                : item.getSuggestedAssigneeId();
        if (assigneeId == null) {
            skipAssignment(command, item, INVALID_CHANGE_REASON, "Assignment target is missing", warnings, counters, now,
                    OptimizationWarningCode.NO_ELIGIBLE_ASSIGNEE);
            return;
        }
        if (!isCurrentWorkItemValid(project, item, workItem)
                || !Objects.equals(workItem.getAssigneeId(), item.getCurrentAssigneeId())
                || !Objects.equals(workItem.getUpdatedAt(), item.getWorkItemUpdatedAtSnapshot())) {
            skipAssignment(command, item, STALE_ITEM_REASON, "Work item assignment changed after optimization generation",
                    warnings, counters, now, OptimizationWarningCode.STALE_ITEM);
            return;
        }
        try {
            ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                    command.userId(), command.groupKeys(), workItem.getReporterId(), workItem.getAssigneeId());
            workItemAuthorizationSupportService.checkRequiredPermissions(
                    permissionSubject, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS, ProjectPermissionKeys.ASSIGN_ISSUES);
            issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);
            Long resolvedAssigneeId = workItemAuthorizationSupportService.resolveAssigneeId(permissionSubject, assigneeId, actorContext);
            if (!Objects.equals(workItem.getAssigneeId(), resolvedAssigneeId)) {
                workItem.setAssigneeId(resolvedAssigneeId);
                workItemService.updateWorkItem(workItem, command.userId());
            }
            item.setAssignmentApplyStatus(OptimizationApplyStatus.APPLIED);
            item.setAssignmentSkippedReason(null);
            item.setAppliedAt(now);
            counters.appliedCount++;
        } catch (DomainException ex) {
            skipAssignment(command, item, PERMISSION_DENIED_REASON, ex.getMessage(), warnings, counters, now,
                    OptimizationWarningCode.PERMISSION_DENIED);
        }
    }

    private void applySchedule(ApplyOptimizationRunCommand command,
                               ProjectPermissionSubject permissionSubject,
                               ProjectEntity project,
                               OptimizationRunItemEntity item,
                               WorkItemEntity workItem,
                               WorkItemPlanEntity activePlan,
                               boolean workItemStale,
                               List<OptimizationRunWarningEntity> warnings,
                               ApplyCounters counters,
                               long now) {
        if (!isActionable(item.getScheduleDecision()) || item.getScheduleApplyStatus() == OptimizationApplyStatus.APPLIED) {
            return;
        }
        counters.actionCount++;
        // Schedule apply writes only the planning table; work_items.due_date remains the business deadline.
        Long plannedStart = item.getScheduleDecision() == OptimizationDecision.OVERRIDDEN
                ? item.getOverridePlannedStart()
                : item.getSuggestedPlannedStart();
        Long plannedEnd = item.getScheduleDecision() == OptimizationDecision.OVERRIDDEN
                ? item.getOverridePlannedEnd()
                : item.getSuggestedPlannedEnd();
        if (plannedStart == null || plannedEnd == null || plannedStart >= plannedEnd) {
            skipSchedule(command, item, INVALID_CHANGE_REASON, "Schedule target is missing or invalid", warnings, counters, now,
                    OptimizationWarningCode.INVALID_OVERRIDE);
            return;
        }
        if (!isCurrentWorkItemValid(project, item, workItem)) {
            skipSchedule(command, item, STALE_ITEM_REASON, "Work item no longer belongs to the project",
                    warnings, counters, now, OptimizationWarningCode.STALE_ITEM);
            return;
        }
        if (workItemStale) {
            skipSchedule(command, item, STALE_ITEM_REASON, "Work item changed after optimization generation",
                    warnings, counters, now, OptimizationWarningCode.STALE_ITEM);
            return;
        }
        // MVP respects manually locked plans and records a skip instead of overwriting them.
        if (Boolean.TRUE.equals(activePlan == null ? Boolean.FALSE : activePlan.getLocked())) {
            skipSchedule(command, item, LOCKED_PLAN_REASON, "Active work item plan is locked", warnings, counters, now,
                    OptimizationWarningCode.LOCKED_PLAN);
            return;
        }
        if (isPlanStale(item, activePlan)) {
            skipSchedule(command, item, STALE_ITEM_REASON, "Work item plan changed after optimization generation",
                    warnings, counters, now, OptimizationWarningCode.STALE_ITEM);
            return;
        }
        try {
            ProjectPermissionEvaluationContext actorContext = workItemAuthorizationSupportService.buildActorContext(
                    command.userId(), command.groupKeys(), workItem.getReporterId(), workItem.getAssigneeId());
            workItemAuthorizationSupportService.checkRequiredPermissions(permissionSubject, actorContext, ProjectPermissionKeys.BROWSE_PROJECTS);
            workItemAuthorizationSupportService.checkScheduleIssuesPermission(permissionSubject, actorContext);
            issueSecurityService.checkSecurityAccessIfNeeded(IssueSecurityAccessContext.from(project, workItem), actorContext);
            WorkItemPlanEntity plan = WorkItemPlanEntity.builder()
                    .tenantId(command.tenantId())
                    .projectId(command.projectId())
                    .workItemId(item.getWorkItemId())
                    .plannedStart(plannedStart)
                    .plannedEnd(plannedEnd)
                    .source(WorkItemPlanSource.OPTIMIZATION)
                    .sourceRunId(command.runId())
                    .locked(false)
                    .build();
            plan.applyCreate(command.userId(), now);
            WorkItemPlanEntity savedPlan = workItemPlanPort.upsertActivePlan(plan);
            if (savedPlan.getId() != null) {
                workItemPlanAllocationPort.replaceForPlan(command.tenantId(), savedPlan.getId(),
                        buildPlanAllocations(command, item, savedPlan));
            }
            item.setScheduleApplyStatus(OptimizationApplyStatus.APPLIED);
            item.setScheduleSkippedReason(null);
            item.setAppliedAt(now);
            counters.appliedCount++;
        } catch (DomainException ex) {
            skipSchedule(command, item, PERMISSION_DENIED_REASON, ex.getMessage(), warnings, counters, now,
                    OptimizationWarningCode.PERMISSION_DENIED);
        }
    }

    private boolean isActionable(OptimizationDecision decision) {
        return decision == OptimizationDecision.ACCEPTED || decision == OptimizationDecision.OVERRIDDEN;
    }

    private List<WorkItemPlanAllocationEntity> buildPlanAllocations(ApplyOptimizationRunCommand command,
                                                                    OptimizationRunItemEntity item,
                                                                    WorkItemPlanEntity plan) {
        String allocationChunksJson = item.getScheduleDecision() == OptimizationDecision.OVERRIDDEN
                ? item.getOverrideAllocationChunksJson()
                : item.getAllocationChunksJson();
        if (allocationChunksJson == null || allocationChunksJson.isBlank()) {
            return List.of();
        }
        List<OptimizationScheduleAllocation> allocations = jsonUtils.fromJsonToList(
                allocationChunksJson, OptimizationScheduleAllocation.class);
        if (allocations == null || allocations.isEmpty()) {
            return List.of();
        }
        return allocations.stream()
                .filter(this::isValidAllocation)
                .map(allocation -> toPlanAllocation(command, item, plan, allocation))
                .toList();
    }

    private boolean isValidAllocation(OptimizationScheduleAllocation allocation) {
        return allocation != null
                && allocation.assigneeId() != null
                && allocation.start() != null
                && allocation.end() != null
                && allocation.start() < allocation.end()
                && allocation.effortMillis() != null
                && allocation.effortMillis() > 0;
    }

    private WorkItemPlanAllocationEntity toPlanAllocation(ApplyOptimizationRunCommand command,
                                                          OptimizationRunItemEntity item,
                                                          WorkItemPlanEntity plan,
                                                          OptimizationScheduleAllocation allocation) {
        return WorkItemPlanAllocationEntity.builder()
                .tenantId(command.tenantId())
                .projectId(command.projectId())
                .workItemPlanId(plan.getId())
                .workItemId(item.getWorkItemId())
                .assigneeId(allocation.assigneeId())
                .startTime(allocation.start())
                .endTime(allocation.end())
                .effortMillis(allocation.effortMillis())
                .source(WorkItemPlanSource.OPTIMIZATION)
                .sourceRunId(command.runId())
                .sourceRunItemId(item.getId())
                .build();
    }

    private boolean isCurrentWorkItemValid(ProjectEntity project, OptimizationRunItemEntity item, WorkItemEntity workItem) {
        return workItem != null
                && Objects.equals(workItem.getTenantId(), project.getTenantId())
                && Objects.equals(workItem.getProjectId(), project.getId())
                && Objects.equals(workItem.getId(), item.getWorkItemId());
    }

    private boolean isPlanStale(OptimizationRunItemEntity item, WorkItemPlanEntity activePlan) {
        // Null snapshot means generation saw no active plan; any current plan is therefore newer user data.
        if (item.getPlanUpdatedAtSnapshot() == null) {
            return activePlan != null;
        }
        return activePlan == null || !Objects.equals(activePlan.getUpdatedAt(), item.getPlanUpdatedAtSnapshot());
    }

    private void skipAssignment(ApplyOptimizationRunCommand command,
                                OptimizationRunItemEntity item,
                                String reason,
                                String message,
                                List<OptimizationRunWarningEntity> warnings,
                                ApplyCounters counters,
                                long now,
                                OptimizationWarningCode code) {
        item.setAssignmentApplyStatus(OptimizationApplyStatus.SKIPPED);
        item.setAssignmentSkippedReason(reason);
        counters.skippedCount++;
        warnings.add(buildWarning(command, item.getWorkItemId(), code, message, now));
    }

    private void skipSchedule(ApplyOptimizationRunCommand command,
                              OptimizationRunItemEntity item,
                              String reason,
                              String message,
                              List<OptimizationRunWarningEntity> warnings,
                              ApplyCounters counters,
                              long now,
                              OptimizationWarningCode code) {
        item.setScheduleApplyStatus(OptimizationApplyStatus.SKIPPED);
        item.setScheduleSkippedReason(reason);
        counters.skippedCount++;
        warnings.add(buildWarning(command, item.getWorkItemId(), code, message, now));
    }

    private OptimizationRunWarningEntity buildWarning(ApplyOptimizationRunCommand command,
                                                      Long workItemId,
                                                      OptimizationWarningCode code,
                                                      String message,
                                                      long now) {
        OptimizationRunWarningEntity warning = OptimizationRunWarningEntity.builder()
                .tenantId(command.tenantId())
                .runId(command.runId())
                .workItemId(workItemId)
                .severity(code == OptimizationWarningCode.LOCKED_PLAN ? "WARN" : "ERROR")
                .code(code.name())
                .message(message)
                .detailsJson(jsonUtils.toJson(Map.of("workItemId", workItemId)))
                .build();
        warning.applyCreate(command.userId(), now);
        return warning;
    }

    private void updateRunStatus(OptimizationRunEntity run, Long userId, long now, ApplyCounters counters) {
        // Any selected accepted/overridden change skipped by stale/locked/permission rules makes the run partial.
        run.setStatus(counters.skippedCount > 0 ? OptimizationRunStatus.PARTIALLY_APPLIED : OptimizationRunStatus.APPLIED);
        run.setAppliedAt(now);
        run.setAppliedBy(userId);
        run.applyUpdate(userId, now);
    }

    private OptimizationRunItemEntity requireRunItem(ApplyOptimizationRunCommand command,
                                                     Map<Long, OptimizationRunItemEntity> itemsByWorkItemId,
                                                     Long workItemId) {
        OptimizationRunItemEntity item = itemsByWorkItemId.get(workItemId);
        if (item == null) {
            log.error("Optimization run item not found for runId={} workItemId={}", command.runId(), workItemId);
            throw new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                    "Optimization run item not found: runId=" + command.runId() + ", workItemId=" + workItemId);
        }
        return item;
    }

    private void validate(ApplyOptimizationRunCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Apply optimization run command is required");
        }
        if (command.tenantId() == null || command.tenantId() <= 0) {
            throw new IllegalArgumentException("tenantId must be a positive number");
        }
        if (command.userId() == null || command.userId() <= 0) {
            throw new IllegalArgumentException("userId must be a positive number");
        }
        if (command.projectId() == null || command.projectId() <= 0) {
            throw new IllegalArgumentException("projectId must be a positive number");
        }
        if (command.runId() == null || command.runId() <= 0) {
            throw new IllegalArgumentException("runId must be a positive number");
        }
        if (!Boolean.TRUE.equals(command.applyAssignment()) && !Boolean.TRUE.equals(command.applySchedule())) {
            throw new IllegalArgumentException("At least one of applyAssignment or applySchedule must be true");
        }
        if (command.workItemIds().isEmpty()) {
            throw new IllegalArgumentException("workItemIds is required");
        }
        if (command.workItemIds().size() > OptimizationConstants.MAX_SELECTED_WORK_ITEM_IDS) {
            throw new IllegalArgumentException(
                    "workItemIds must not exceed " + OptimizationConstants.MAX_SELECTED_WORK_ITEM_IDS + " items"
            );
        }
        if (new LinkedHashSet<>(command.workItemIds()).size() != command.workItemIds().size()) {
            throw new IllegalArgumentException("workItemIds must not contain duplicates");
        }
        if (command.workItemIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("workItemIds must contain positive numbers only");
        }
    }

    private static final class ApplyCounters {
        private int actionCount;
        private int appliedCount;
        private int skippedCount;
    }
}
