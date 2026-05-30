/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import serp.project.pmcore.domain.optimization.model.OptimizationAlgorithmOptions;
import serp.project.pmcore.domain.optimization.model.OptimizationAssignmentSuggestion;
import serp.project.pmcore.domain.optimization.model.OptimizationBuilderInput;
import serp.project.pmcore.domain.optimization.model.OptimizationConstraintViolation;
import serp.project.pmcore.domain.optimization.model.OptimizationGenerationResult;
import serp.project.pmcore.domain.optimization.model.OptimizationProjectModel;
import serp.project.pmcore.domain.optimization.model.OptimizationRunSummary;
import serp.project.pmcore.domain.optimization.model.OptimizationScheduleSuggestion;
import serp.project.pmcore.domain.optimization.service.IOptimizationRunGenerator;
import serp.project.pmcore.domain.optimization.service.assignment.GreedyAssignmentPolicy;
import serp.project.pmcore.domain.optimization.service.schedule.GreedySchedulingPolicy;
import serp.project.pmcore.domain.optimization.service.summary.OptimizationSummaryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GreedyOptimizationRunGenerator implements IOptimizationRunGenerator {
    private final GreedyAssignmentPolicy assignmentPolicy;
    private final GreedySchedulingPolicy schedulingPolicy;
    private final OptimizationSummaryBuilder summaryBuilder;

    @Override
    public OptimizationGenerationResult generate(OptimizationProjectModel projectModel, OptimizationBuilderInput input) {
        List<OptimizationConstraintViolation> warnings = new ArrayList<>(projectModel.warnings());
        OptimizationAlgorithmOptions options = new OptimizationAlgorithmOptions(input.intent());
        Map<Long, OptimizationAssignmentSuggestion> assignments = assignmentPolicy.generateAssignments(
                projectModel, options, warnings);
        Map<Long, OptimizationScheduleSuggestion> schedules = schedulingPolicy.generateSchedules(
                projectModel, options, assignments, warnings);
        OptimizationRunSummary summary = summaryBuilder.buildSummary(projectModel, assignments, schedules, warnings);
        return new OptimizationGenerationResult(assignments, schedules, warnings, summary);
    }
}
