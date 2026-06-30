/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.generate;

import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.optimization.enums.OptimizationChangeScope;
import serp.project.pmcore.domain.optimization.enums.OptimizationObjective;
import serp.project.pmcore.domain.optimization.service.OptimizationObjectiveAlgorithmMapper;

import java.util.List;

public record GenerateOptimizationRunCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        String scope,
        String algorithmKey,
        OptimizationObjective objective,
        OptimizationChangeScope changeScope,
        Long planningStart,
        Long planningEnd,
        List<Long> selectedWorkItemIds
) implements ICommand<OptimizationRunReviewView> {
    public GenerateOptimizationRunCommand {
        selectedWorkItemIds = selectedWorkItemIds == null ? List.of() : List.copyOf(selectedWorkItemIds);
        objective = objective == null ? OptimizationObjective.BALANCED_WORKLOAD : objective;
        algorithmKey = OptimizationObjectiveAlgorithmMapper.algorithmKeyFor(objective);
        changeScope = changeScope == null ? OptimizationChangeScope.ASSIGNMENT_AND_SCHEDULE : changeScope;
    }
}
