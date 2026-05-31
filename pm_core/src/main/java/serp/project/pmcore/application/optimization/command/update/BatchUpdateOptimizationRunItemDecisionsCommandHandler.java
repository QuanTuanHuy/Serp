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
import serp.project.pmcore.application.optimization.support.OptimizationRunWarningAuditService;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationDependencyEdge;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunIntent;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.service.IOptimizationProjectModelBuilder;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchUpdateOptimizationRunItemDecisionsCommandHandler
        implements ICommandHandler<BatchUpdateOptimizationRunItemDecisionsCommand, OptimizationRunReviewView> {

    private final OptimizationRunGuard optimizationRunGuard;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final IOptimizationProjectModelBuilder optimizationProjectModelBuilder;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    private final OptimizationRunWarningAuditService optimizationRunWarningAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OptimizationRunReviewView handle(BatchUpdateOptimizationRunItemDecisionsCommand command) {
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
        Map<Long, OptimizationRunItemEntity> itemsByWorkItemId = items.stream()
                .collect(Collectors.toMap(OptimizationRunItemEntity::getWorkItemId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Map<Long, BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision> decisionsByWorkItemId = command.items().stream()
                .collect(Collectors.toMap(
                        BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision::workItemId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<OptimizationRunItemEntity> selectedItems = decisionsByWorkItemId.keySet().stream()
                .map(workItemId -> requireRunItem(command, itemsByWorkItemId, workItemId))
                .sorted(Comparator.comparing(OptimizationRunItemEntity::getWorkItemId))
                .toList();

        OptimizationProjectModel projectModel = needsModelValidation(command)
                ? buildCurrentProjectModel(run, items)
                : null;
        decisionsByWorkItemId.values().forEach(decision ->
                validateDecisionBasics(command, run, decision, projectModel));
        decisionsByWorkItemId.values().forEach(decision ->
                validateScheduleDependencies(command, decision, projectModel, itemsByWorkItemId, decisionsByWorkItemId));

        long now = System.currentTimeMillis();
        for (BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision : decisionsByWorkItemId.values()) {
            OptimizationRunItemEntity item = itemsByWorkItemId.get(decision.workItemId());
            applyDecision(command, decision, item, now);
        }
        optimizationRunItemPort.saveAll(selectedItems);

        List<OptimizationRunItemEntity> savedItems = optimizationRunItemPort.listByRunId(command.tenantId(), command.runId());
        List<OptimizationRunWarningEntity> warnings = optimizationRunWarningPort.listByRunId(command.tenantId(), command.runId());
        return optimizationRunReviewAssembler.toView(run, savedItems, warnings);
    }

    private void validateDecisionBasics(BatchUpdateOptimizationRunItemDecisionsCommand command,
                                        OptimizationRunEntity run,
                                        BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision,
                                        OptimizationProjectModel projectModel) {
        if (decision.assignmentDecision() == OptimizationDecision.OVERRIDDEN) {
            if (decision.overrideAssigneeId() == null || decision.overrideAssigneeId() <= 0) {
                rejectInvalidOverride(command, decision.workItemId(),
                        "overrideAssigneeId is required when assignmentDecision is OVERRIDDEN");
            }
            if (!isGeneratedCandidate(projectModel, decision.workItemId(), decision.overrideAssigneeId())) {
                rejectInvalidOverride(command, decision.workItemId(),
                        "overrideAssigneeId must be one of the generated candidate assignees");
            }
        }
        if (decision.scheduleDecision() == OptimizationDecision.OVERRIDDEN) {
            if (decision.overridePlannedStart() == null || decision.overridePlannedEnd() == null) {
                rejectInvalidOverride(command, decision.workItemId(),
                        "overridePlannedStart and overridePlannedEnd are required when scheduleDecision is OVERRIDDEN");
            }
            if (decision.overridePlannedStart() >= decision.overridePlannedEnd()) {
                rejectInvalidOverride(command, decision.workItemId(), "overridePlannedStart must be before overridePlannedEnd");
            }
            if (decision.overridePlannedStart() < run.getPlanningStart()
                    || decision.overridePlannedEnd() > run.getPlanningEnd()) {
                rejectInvalidOverride(command, decision.workItemId(),
                        "override planned range must stay within the optimization planning range");
            }
        }
    }

    private void validateScheduleDependencies(BatchUpdateOptimizationRunItemDecisionsCommand command,
                                              BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision,
                                              OptimizationProjectModel projectModel,
                                              Map<Long, OptimizationRunItemEntity> itemsByWorkItemId,
                                              Map<Long, BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision> decisionsByWorkItemId) {
        if (decision.scheduleDecision() != OptimizationDecision.OVERRIDDEN || projectModel == null) {
            return;
        }
        for (OptimizationDependencyEdge edge : projectModel.dependencyGraph().internalEdges()) {
            if (Objects.equals(edge.successorId(), decision.workItemId())) {
                Long predecessorEnd = effectivePlannedEnd(edge.predecessorId(), itemsByWorkItemId, decisionsByWorkItemId);
                if (predecessorEnd != null && predecessorEnd > decision.overridePlannedStart()) {
                    rejectInvalidOverride(command, decision.workItemId(),
                            "override violates hard dependency: predecessorId=" + edge.predecessorId());
                }
            }
            if (Objects.equals(edge.predecessorId(), decision.workItemId())) {
                Long successorStart = effectivePlannedStart(edge.successorId(), itemsByWorkItemId, decisionsByWorkItemId);
                if (successorStart != null && decision.overridePlannedEnd() > successorStart) {
                    rejectInvalidOverride(command, decision.workItemId(),
                            "override violates hard dependency: successorId=" + edge.successorId());
                }
            }
        }
    }

    private Long effectivePlannedStart(Long workItemId,
                                       Map<Long, OptimizationRunItemEntity> itemsByWorkItemId,
                                       Map<Long, BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision> decisionsByWorkItemId) {
        OptimizationRunItemEntity item = itemsByWorkItemId.get(workItemId);
        if (item == null) {
            return null;
        }
        BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision = decisionsByWorkItemId.get(workItemId);
        OptimizationDecision scheduleDecision = finalScheduleDecision(item, decision);
        if (scheduleDecision == OptimizationDecision.OVERRIDDEN) {
            return decision != null && decision.scheduleDecision() == OptimizationDecision.OVERRIDDEN
                    ? decision.overridePlannedStart()
                    : item.getOverridePlannedStart();
        }
        if (scheduleDecision == OptimizationDecision.REJECTED) {
            return item.getCurrentPlannedStart();
        }
        return item.getSuggestedPlannedStart();
    }

    private Long effectivePlannedEnd(Long workItemId,
                                     Map<Long, OptimizationRunItemEntity> itemsByWorkItemId,
                                     Map<Long, BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision> decisionsByWorkItemId) {
        OptimizationRunItemEntity item = itemsByWorkItemId.get(workItemId);
        if (item == null) {
            return null;
        }
        BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision = decisionsByWorkItemId.get(workItemId);
        OptimizationDecision scheduleDecision = finalScheduleDecision(item, decision);
        if (scheduleDecision == OptimizationDecision.OVERRIDDEN) {
            return decision != null && decision.scheduleDecision() == OptimizationDecision.OVERRIDDEN
                    ? decision.overridePlannedEnd()
                    : item.getOverridePlannedEnd();
        }
        if (scheduleDecision == OptimizationDecision.REJECTED) {
            return item.getCurrentPlannedEnd();
        }
        return item.getSuggestedPlannedEnd();
    }

    private OptimizationDecision finalScheduleDecision(OptimizationRunItemEntity item,
                                                       BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision) {
        if (decision != null && decision.scheduleDecision() != null) {
            return decision.scheduleDecision();
        }
        return item.getScheduleDecision();
    }

    private void applyDecision(BatchUpdateOptimizationRunItemDecisionsCommand command,
                               BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision decision,
                               OptimizationRunItemEntity item,
                               long now) {
        if (decision.assignmentDecision() != null) {
            item.setAssignmentDecision(decision.assignmentDecision());
            item.setOverrideAssigneeId(decision.assignmentDecision() == OptimizationDecision.OVERRIDDEN
                    ? decision.overrideAssigneeId()
                    : null);
        }
        if (decision.scheduleDecision() != null) {
            item.setScheduleDecision(decision.scheduleDecision());
            if (decision.scheduleDecision() == OptimizationDecision.OVERRIDDEN) {
                item.setOverridePlannedStart(decision.overridePlannedStart());
                item.setOverridePlannedEnd(decision.overridePlannedEnd());
            } else {
                item.setOverridePlannedStart(null);
                item.setOverridePlannedEnd(null);
            }
        }
        item.applyUpdate(command.userId(), now);
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
        List<Long> selectedWorkItemIds = items.stream()
                .map(OptimizationRunItemEntity::getWorkItemId)
                .toList();
        OptimizationRunIntent intent = new OptimizationRunIntent(
                run.getAlgorithmKey(),
                OptimizationObjective.valueOf(run.getObjective()),
                OptimizationChangeScope.valueOf(run.getChangeScope())
        );
        return optimizationProjectModelBuilder.build(new OptimizationBuilderInput(
                run.getTenantId(),
                run.getProjectId(),
                selectedWorkItemIds,
                run.getPlanningStart(),
                run.getPlanningEnd(),
                intent
        ));
    }

    private boolean needsModelValidation(BatchUpdateOptimizationRunItemDecisionsCommand command) {
        return command.items().stream()
                .anyMatch(item -> item.assignmentDecision() == OptimizationDecision.OVERRIDDEN
                        || item.scheduleDecision() == OptimizationDecision.OVERRIDDEN);
    }

    private OptimizationRunItemEntity requireRunItem(BatchUpdateOptimizationRunItemDecisionsCommand command,
                                                     Map<Long, OptimizationRunItemEntity> itemsByWorkItemId,
                                                     Long workItemId) {
        OptimizationRunItemEntity item = itemsByWorkItemId.get(workItemId);
        if (item == null) {
            throw new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                    "Optimization run item not found: runId=" + command.runId() + ", workItemId=" + workItemId);
        }
        return item;
    }

    private void rejectInvalidOverride(BatchUpdateOptimizationRunItemDecisionsCommand command,
                                       Long workItemId,
                                       String message) {
        optimizationRunWarningAuditService.recordInvalidOverrideWarning(
                command.tenantId(),
                command.userId(),
                command.runId(),
                workItemId,
                message
        );
        throw new IllegalArgumentException(message);
    }

    private void validate(BatchUpdateOptimizationRunItemDecisionsCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Batch update optimization run item decisions command is required");
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
        if (command.items().isEmpty()) {
            throw new IllegalArgumentException("items is required");
        }
        if (command.items().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("items must not contain null values");
        }
        List<Long> workItemIds = command.items().stream()
                .map(BatchUpdateOptimizationRunItemDecisionsCommand.ItemDecision::workItemId)
                .toList();
        if (workItemIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("workItemIds must contain positive numbers only");
        }
        if (new LinkedHashSet<>(workItemIds).size() != workItemIds.size()) {
            throw new IllegalArgumentException("workItemIds must not contain duplicates");
        }
    }
}
