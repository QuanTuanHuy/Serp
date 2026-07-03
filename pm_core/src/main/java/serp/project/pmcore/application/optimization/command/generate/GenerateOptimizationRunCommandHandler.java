/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.generate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewAssembler;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.optimization.constant.OptimizationConstants;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.entity.WorkItemPlanEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationCapability;
import serp.project.pmcore.domain.optimization.enums.OptimizationApplyStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.enums.OptimizationSolverStatus;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmDescriptor;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationProblem;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunIntent;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationSolution;
import serp.project.pmcore.domain.optimization.model.OptimizationWorkItem;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithm;
import serp.project.pmcore.domain.optimization.service.IOptimizationAlgorithmRegistry;
import serp.project.pmcore.domain.optimization.service.IOptimizationProjectModelBuilder;
import serp.project.pmcore.domain.optimization.service.OptimizationObjectiveAlgorithmMapper;
import serp.project.pmcore.domain.optimization.service.OptimizationSolutionValidator;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateOptimizationRunCommandHandler
        implements ICommandHandler<GenerateOptimizationRunCommand, OptimizationRunReviewView> {
    private final IOptimizationProjectModelBuilder optimizationProjectModelBuilder;
    private final IOptimizationAlgorithmRegistry optimizationAlgorithmRegistry;
    private final IOptimizationRunPort optimizationRunPort;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;
    private final OptimizationSolutionValidator optimizationSolutionValidator;
    private final JsonUtils jsonUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OptimizationRunReviewView handle(GenerateOptimizationRunCommand command) {
        validate(command);
        String algorithmKey = normalizeAlgorithmKey(command.objective());
        OptimizationRunIntent intent = new OptimizationRunIntent(
                algorithmKey,
                command.objective(),
                command.changeScope()
        );
        OptimizationBuilderInput input = new OptimizationBuilderInput(
                command.tenantId(),
                command.projectId(),
                command.selectedWorkItemIds(),
                command.planningStart(),
                command.planningEnd(),
                intent
        );
        OptimizationProjectModel projectModel = optimizationProjectModelBuilder.build(input);
        IOptimizationAlgorithm algorithm = optimizationAlgorithmRegistry.resolve(algorithmKey);
        validateCapabilities(intent, algorithm.descriptor());
        OptimizationSolution solution = algorithm.solve(new OptimizationProblem(projectModel, input));
        solution = optimizationSolutionValidator.validate(new OptimizationProblem(projectModel, input), solution);

        long now = System.currentTimeMillis();
        OptimizationRunEntity run = OptimizationRunEntity.builder()
                .tenantId(command.tenantId())
                .projectId(command.projectId())
                .scope(normalizeScope(command.scope()))
                .objective(command.objective().name())
                .changeScope(command.changeScope().name())
                .status(OptimizationRunStatus.GENERATED)
                .planningStart(command.planningStart())
                .planningEnd(command.planningEnd())
                .selectedWorkItemCount(command.selectedWorkItemIds().size())
                .summaryJson(jsonUtils.toJson(solution.summary()))
                .algorithmKey(solution.algorithm().key())
                .algorithmVersion(solution.algorithm().version())
                .solverStatus(solution.solverStatus() == null
                        ? OptimizationSolverStatus.FEASIBLE.name()
                        : solution.solverStatus().name())
                .objectiveScore(solution.objectiveScore())
                .build();
        run.applyCreate(command.userId(), now);
        OptimizationRunEntity savedRun = optimizationRunPort.save(run);

        List<OptimizationRunItemEntity> items = buildRunItems(command, savedRun.getId(), projectModel, solution, now);
        List<OptimizationRunItemEntity> savedItems = optimizationRunItemPort.saveAll(items);
        List<OptimizationRunWarningEntity> warnings = buildWarnings(command, savedRun.getId(), solution.warnings(), now);
        List<OptimizationRunWarningEntity> savedWarnings = warnings.isEmpty()
                ? List.of()
                : optimizationRunWarningPort.saveAll(warnings);

        log.info("Generated optimization run id={} tenantId={} projectId={} items={} warnings={}",
                savedRun.getId(), command.tenantId(), command.projectId(), savedItems.size(), savedWarnings.size());
        return optimizationRunReviewAssembler.toView(savedRun, savedItems, savedWarnings);
    }

    private List<OptimizationRunItemEntity> buildRunItems(GenerateOptimizationRunCommand command,
                                                          Long runId,
                                                          OptimizationProjectModel projectModel,
                                                          OptimizationSolution solution,
                                                          long now) {
        List<OptimizationRunItemEntity> items = new ArrayList<>();
        for (OptimizationWorkItem item : projectModel.workItems()) {
            WorkItemEntity workItem = item.workItem();
            WorkItemPlanEntity activePlan = item.activePlan();
            OptimizationAssignmentSuggestion assignment = solution.assignmentSuggestions().get(workItem.getId());
            OptimizationScheduleSuggestion schedule = solution.scheduleSuggestions().get(workItem.getId());
            Long suggestedAssigneeId = assignment == null ? workItem.getAssigneeId() : assignment.suggestedAssigneeId();
            List<String> violations = new ArrayList<>();
            if (assignment != null) {
                assignment.violations().forEach(violation -> violations.add(formatViolation(violation)));
            }
            if (schedule != null) {
                schedule.violations().forEach(violation -> violations.add(formatViolation(violation)));
            }

            OptimizationRunItemEntity runItem = OptimizationRunItemEntity.builder()
                    .tenantId(command.tenantId())
                    .runId(runId)
                    .projectId(command.projectId())
                    .workItemId(workItem.getId())
                    .workItemUpdatedAtSnapshot(workItem.getUpdatedAt())
                    .planUpdatedAtSnapshot(activePlan == null ? null : activePlan.getUpdatedAt())
                    .currentAssigneeId(workItem.getAssigneeId())
                    .suggestedAssigneeId(suggestedAssigneeId)
                    .currentPlannedStart(activePlan == null ? null : activePlan.getPlannedStart())
                    .currentPlannedEnd(activePlan == null ? null : activePlan.getPlannedEnd())
                    .suggestedPlannedStart(schedule == null ? null : schedule.plannedStart())
                    .suggestedPlannedEnd(schedule == null ? null : schedule.plannedEnd())
                    .currentDueDate(workItem.getDueDate())
                    .assignmentDecision(Objects.equals(workItem.getAssigneeId(), suggestedAssigneeId)
                            ? OptimizationDecision.ACCEPTED : OptimizationDecision.PENDING)
                    .scheduleDecision(schedule == null ? OptimizationDecision.ACCEPTED : OptimizationDecision.PENDING)
                    .assignmentApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                    .scheduleApplyStatus(OptimizationApplyStatus.NOT_APPLIED)
                    .score(BigDecimal.valueOf(item.priorityScore().score())
                            .setScale(OptimizationConstants.SCORE_DECIMAL_SCALE, RoundingMode.HALF_UP))
                    .cost(BigDecimal.valueOf(assignment == null ? 0D : assignment.cost())
                            .setScale(OptimizationConstants.SCORE_DECIMAL_SCALE, RoundingMode.HALF_UP))
                    .confidence(schedule == null ? item.duration().confidence().name() : schedule.confidence().name())
                    .assignmentReasonsJson(jsonUtils.toJson(assignment == null ? List.of() : assignment.reasons()))
                    .scheduleReasonsJson(jsonUtils.toJson(schedule == null ? List.of() : schedule.reasons()))
                    .violationsJson(jsonUtils.toJson(violations))
                    .allocationChunksJson(jsonUtils.toJson(schedule == null ? List.of() : schedule.allocations()))
                    .build();
            runItem.applyCreate(command.userId(), now);
            items.add(runItem);
        }
        return items;
    }

    private List<OptimizationRunWarningEntity> buildWarnings(GenerateOptimizationRunCommand command,
                                                             Long runId,
                                                             List<OptimizationConstraintViolation> warnings,
                                                             long now) {
        List<OptimizationRunWarningEntity> entities = new ArrayList<>();
        for (OptimizationConstraintViolation warning : warnings) {
            OptimizationRunWarningEntity entity = OptimizationRunWarningEntity.builder()
                    .tenantId(command.tenantId())
                    .runId(runId)
                    .workItemId(warning.workItemId())
                    .severity(severityOf(warning))
                    .code(warning.code().name())
                    .message(warning.message())
                    .detailsJson(jsonUtils.toJson(warning.details() == null ? List.of() : List.of(warning.details())))
                    .build();
            entity.applyCreate(command.userId(), now);
            entities.add(entity);
        }
        return entities;
    }

    private String severityOf(OptimizationConstraintViolation warning) {
        return switch (warning.code()) {
            case DEPENDENCY_CYCLE, DEPENDENCY_VIOLATION, NO_ELIGIBLE_ASSIGNEE, OVER_CAPACITY -> "ERROR";
            case EXTERNAL_DEPENDENCY -> "INFO";
            default -> "WARN";
        };
    }

    private String formatViolation(OptimizationConstraintViolation violation) {
        return violation.code().name() + ": " + violation.message()
                + (violation.details() == null ? "" : " (" + violation.details() + ")");
    }

    private String normalizeScope(String scope) {
        return scope == null || scope.isBlank() ? OptimizationConstants.DEFAULT_SCOPE : scope;
    }

    private String normalizeAlgorithmKey(OptimizationObjective objective) {
        return OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(objective);
    }

    private void validateCapabilities(OptimizationRunIntent intent, OptimizationAlgorithmDescriptor descriptor) {
        if (intent.changeScope().includesAssignment()
                && !descriptor.capabilities().contains(OptimizationCapability.ASSIGNMENT)) {
            throw new IllegalArgumentException("Optimization algorithm does not support assignment: " + descriptor.key());
        }
        if (intent.changeScope().includesScheduling()
                && !descriptor.capabilities().contains(OptimizationCapability.SCHEDULING)) {
            throw new IllegalArgumentException("Optimization algorithm does not support scheduling: " + descriptor.key());
        }
    }

    private void validate(GenerateOptimizationRunCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Generate optimization run command is required");
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
        if (command.planningStart() == null || command.planningEnd() == null
                || command.planningStart() >= command.planningEnd()) {
            throw new IllegalArgumentException("planningStart must be before planningEnd");
        }
        if (command.selectedWorkItemIds().isEmpty()) {
            throw new IllegalArgumentException("selectedWorkItemIds is required");
        }
        if (command.selectedWorkItemIds().size() > OptimizationConstants.MAX_SELECTED_WORK_ITEM_IDS) {
            throw new IllegalArgumentException(
                    "selectedWorkItemIds must not exceed " + OptimizationConstants.MAX_SELECTED_WORK_ITEM_IDS + " items"
            );
        }
        if (new LinkedHashSet<>(command.selectedWorkItemIds()).size() != command.selectedWorkItemIds().size()) {
            throw new IllegalArgumentException("selectedWorkItemIds must not contain duplicates");
        }
        if (command.selectedWorkItemIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("selectedWorkItemIds must contain positive numbers only");
        }
        if (command.objective() == null) {
            throw new IllegalArgumentException("objective is required");
        }
        if (command.changeScope() == null) {
            throw new IllegalArgumentException("changeScope is required");
        }
    }
}
