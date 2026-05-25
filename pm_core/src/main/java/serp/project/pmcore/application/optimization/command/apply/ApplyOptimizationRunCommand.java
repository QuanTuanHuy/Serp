/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.apply;

import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record ApplyOptimizationRunCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        Long runId,
        Boolean applyAssignment,
        Boolean applySchedule,
        List<Long> workItemIds,
        Set<String> groupKeys
) implements ICommand<OptimizationRunReviewView> {

    public ApplyOptimizationRunCommand {
        workItemIds = workItemIds == null ? List.of() : List.copyOf(workItemIds);
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }
}
