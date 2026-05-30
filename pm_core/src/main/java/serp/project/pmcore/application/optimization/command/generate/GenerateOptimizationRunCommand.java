/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.generate;

import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.optimization.constant.OptimizationAlgorithmKeys;
import serp.project.pmcore.domain.optimization.enums.OptimizationMode;

import java.util.List;

public record GenerateOptimizationRunCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        String scope,
        String algorithmKey,
        OptimizationMode mode,
        Long planningStart,
        Long planningEnd,
        Boolean allowReassignment,
        Boolean allowScheduleChanges,
        List<Long> selectedWorkItemIds
) implements ICommand<OptimizationRunReviewView> {
    public GenerateOptimizationRunCommand {
        selectedWorkItemIds = selectedWorkItemIds == null ? List.of() : List.copyOf(selectedWorkItemIds);
        algorithmKey = algorithmKey == null || algorithmKey.isBlank()
                ? OptimizationAlgorithmKeys.GREEDY_BALANCED
                : algorithmKey;
        mode = mode == null ? OptimizationMode.BALANCED_WORKLOAD : mode;
        allowReassignment = Boolean.TRUE.equals(allowReassignment);
        allowScheduleChanges = Boolean.TRUE.equals(allowScheduleChanges);
    }
}
