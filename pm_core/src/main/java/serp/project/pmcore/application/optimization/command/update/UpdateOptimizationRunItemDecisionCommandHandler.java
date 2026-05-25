/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationWarningCode;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyEdge;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;
import serp.project.pmcore.domain.optimization.service.IOptimizationProjectModelBuilder;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpdateOptimizationRunItemDecisionCommandHandler
        implements ICommandHandler<UpdateOptimizationRunItemDecisionCommand, OptimizationRunReviewView> {

    private final OptimizationRunGuard optimizationRunGuard;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final IOptimizationProjectModelBuilder optimizationProjectModelBuilder;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    private final JsonUtils jsonUtils;

    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = IllegalArgumentException.class)
    public OptimizationRunReviewView handle(UpdateOptimizationRunItemDecisionCommand command) {
        validate(command);
        OptimizationRunEntity run = optimizationRunGuard.requireRunInProject(
                command.tenantId(),
                command.projectId(),
                command.runId()
        );
        optimizationRunGuard.ensureStatus(
                run,
                Set.of(OptimizationRunStatus.GENERATED, OptimizationRunStatus.PARTIALLY_APPLIED),
                "updated"
        );

        List<OptimizationRunItemEntity> items = optimizationRunItemPort.listByRunId(command.tenantId(), command.runId());
        OptimizationRunItemEntity item = items.stream()
                .filter(candidate -> Objects.equals(candidate.getWorkItemId(), command.workItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                        "Optimization run item not found: runId=" + command.runId()
                                + ", workItemId=" + command.workItemId()));

        // Rebuild only for overrides because candidate/dependency validation needs the current model graph.
        OptimizationProjectModel projectModel = needsModelValidation(command)
                ? buildCurrentProjectModel(run, items)
                : null;

        applyAssignmentDecision(command, run, item, projectModel);
        applyScheduleDecision(command, run, item, projectModel, items);

        item.applyUpdate(command.userId(), System.currentTimeMillis());
        optimizationRunItemPort.save(item);

        List<OptimizationRunItemEntity> savedItems = optimizationRunItemPort.listByRunId(command.tenantId(), command.runId());
        List<OptimizationRunWarningEntity> warnings = optimizationRunWarningPort.listByRunId(command.tenantId(), command.runId());
        return optimizationRunReviewAssembler.toView(run, savedItems, warnings);
    }

    private void applyAssignmentDecision(UpdateOptimizationRunItemDecisionCommand command,
                                         OptimizationRunEntity run,
                                         OptimizationRunItemEntity item,
                                         OptimizationProjectModel projectModel) {
        if (command.assignmentDecision() == null) {
            return;
        }
        item.setAssignmentDecision(command.assignmentDecision());
        if (command.assignmentDecision() == OptimizationDecision.OVERRIDDEN) {
            if (command.overrideAssigneeId() == null || command.overrideAssigneeId() <= 0) {
                rejectInvalidOverride(command, "overrideAssigneeId is required when assignmentDecision is OVERRIDDEN");
            }
            // MVP restricts overrides to generated candidates until project membership validation exists.
            if (!isGeneratedCandidate(projectModel, command.workItemId(), command.overrideAssigneeId())) {
                rejectInvalidOverride(command, "overrideAssigneeId must be one of the generated candidate assignees");
            }
            item.setOverrideAssigneeId(command.overrideAssigneeId());
            return;
        }
        item.setOverrideAssigneeId(null);
    }

    private void applyScheduleDecision(UpdateOptimizationRunItemDecisionCommand command,
                                       OptimizationRunEntity run,
                                       OptimizationRunItemEntity item,
                                       OptimizationProjectModel projectModel,
                                       List<OptimizationRunItemEntity> items) {
        if (command.scheduleDecision() == null) {
            return;
        }
        item.setScheduleDecision(command.scheduleDecision());
        if (command.scheduleDecision() == OptimizationDecision.OVERRIDDEN) {
            validateScheduleOverride(command, run, item, projectModel, items);
            item.setOverridePlannedStart(command.overridePlannedStart());
            item.setOverridePlannedEnd(command.overridePlannedEnd());
            return;
        }
        item.setOverridePlannedStart(null);
        item.setOverridePlannedEnd(null);
    }

    private void validateScheduleOverride(UpdateOptimizationRunItemDecisionCommand command,
                                          OptimizationRunEntity run,
                                          OptimizationRunItemEntity item,
                                          OptimizationProjectModel projectModel,
                                          List<OptimizationRunItemEntity> items) {
        if (command.overridePlannedStart() == null || command.overridePlannedEnd() == null) {
            rejectInvalidOverride(command, "overridePlannedStart and overridePlannedEnd are required when scheduleDecision is OVERRIDDEN");
        }
        if (command.overridePlannedStart() >= command.overridePlannedEnd()) {
            rejectInvalidOverride(command, "overridePlannedStart must be before overridePlannedEnd");
        }
        if (command.overridePlannedStart() < run.getPlanningStart() || command.overridePlannedEnd() > run.getPlanningEnd()) {
            rejectInvalidOverride(command, "override planned range must stay within the optimization planning range");
        }
        // Hard dependency edges cannot be softened by review overrides; users must adjust dates to keep order valid.
        Map<Long, OptimizationRunItemEntity> itemsByWorkItemId = items.stream()
                .collect(Collectors.toMap(OptimizationRunItemEntity::getWorkItemId, current -> current, (left, right) -> left, LinkedHashMap::new));
        for (OptimizationDependencyEdge edge : projectModel.dependencyGraph().internalEdges()) {
            if (Objects.equals(edge.successorId(), command.workItemId())) {
                OptimizationRunItemEntity predecessor = itemsByWorkItemId.get(edge.predecessorId());
                Long predecessorEnd = effectivePlannedEnd(predecessor);
                if (predecessorEnd != null && predecessorEnd > command.overridePlannedStart()) {
                    rejectInvalidOverride(command, "override violates hard dependency: predecessorId=" + edge.predecessorId());
                }
            }
            if (Objects.equals(edge.predecessorId(), command.workItemId())) {
                OptimizationRunItemEntity successor = itemsByWorkItemId.get(edge.successorId());
                Long successorStart = effectivePlannedStart(successor);
                if (successorStart != null && command.overridePlannedEnd() > successorStart) {
                    rejectInvalidOverride(command, "override violates hard dependency: successorId=" + edge.successorId());
                }
            }
        }
    }

    private Long effectivePlannedStart(OptimizationRunItemEntity item) {
        if (item == null) {
            return null;
        }
        if (item.getScheduleDecision() == OptimizationDecision.OVERRIDDEN) {
            return item.getOverridePlannedStart();
        }
        if (item.getScheduleDecision() == OptimizationDecision.REJECTED) {
            return item.getCurrentPlannedStart();
        }
        return item.getSuggestedPlannedStart();
    }

    private Long effectivePlannedEnd(OptimizationRunItemEntity item) {
        if (item == null) {
            return null;
        }
        if (item.getScheduleDecision() == OptimizationDecision.OVERRIDDEN) {
            return item.getOverridePlannedEnd();
        }
        if (item.getScheduleDecision() == OptimizationDecision.REJECTED) {
            return item.getCurrentPlannedEnd();
        }
        return item.getSuggestedPlannedEnd();
    }

    private boolean isGeneratedCandidate(OptimizationProjectModel projectModel, Long workItemId, Long assigneeId) {
        if (projectModel == null) {
            return false;
        }
        return projectModel.workItems().stream()
                .filter(item -> Objects.equals(item.workItem().getId(), workItemId))
                .map(OptimizationWorkItem::candidateAssignees)
                .flatMap(List::stream)
                .anyMatch(candidate -> Objects.equals(candidate.candidateId(), assigneeId));
    }

    private OptimizationProjectModel buildCurrentProjectModel(OptimizationRunEntity run,
                                                              List<OptimizationRunItemEntity> items) {
        OptimizationMode mode = OptimizationMode.valueOf(run.getMode());
        List<Long> selectedWorkItemIds = items.stream()
                .map(OptimizationRunItemEntity::getWorkItemId)
                .toList();
        return optimizationProjectModelBuilder.build(new OptimizationBuilderInput(
                run.getTenantId(),
                run.getProjectId(),
                selectedWorkItemIds,
                run.getPlanningStart(),
                run.getPlanningEnd(),
                run.getAllowReassignment(),
                run.getAllowScheduleChanges(),
                mode
        ));
    }

    private boolean needsModelValidation(UpdateOptimizationRunItemDecisionCommand command) {
        return command.assignmentDecision() == OptimizationDecision.OVERRIDDEN
                || command.scheduleDecision() == OptimizationDecision.OVERRIDDEN;
    }

    private void rejectInvalidOverride(UpdateOptimizationRunItemDecisionCommand command, String message) {
        long now = System.currentTimeMillis();
        // Keep an audit warning even though the PATCH itself is rejected.
        OptimizationRunWarningEntity warning = OptimizationRunWarningEntity.builder()
                .tenantId(command.tenantId())
                .runId(command.runId())
                .workItemId(command.workItemId())
                .severity("ERROR")
                .code(OptimizationWarningCode.INVALID_OVERRIDE.name())
                .message(message)
                .detailsJson(jsonUtils.toJson(Map.of("workItemId", command.workItemId())))
                .build();
        warning.applyCreate(command.userId(), now);
        optimizationRunWarningPort.saveAll(List.of(warning));
        throw new IllegalArgumentException(message);
    }

    private void validate(UpdateOptimizationRunItemDecisionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Update optimization run item decision command is required");
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
        if (command.workItemId() == null || command.workItemId() <= 0) {
            throw new IllegalArgumentException("workItemId must be a positive number");
        }
    }
}
