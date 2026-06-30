/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.update;

import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;

import java.util.List;

public record BatchUpdateOptimizationRunItemDecisionsCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        Long runId,
        List<ItemDecision> items
) implements ICommand<OptimizationRunReviewView> {

    public BatchUpdateOptimizationRunItemDecisionsCommand {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public record ItemDecision(
            Long workItemId,
            OptimizationDecision assignmentDecision,
            OptimizationDecision scheduleDecision,
            Long overrideAssigneeId,
            Long overridePlannedStart,
            Long overridePlannedEnd,
            List<AllocationOverride> overrideAllocationChunks
    ) {
        public ItemDecision {
            overrideAllocationChunks = overrideAllocationChunks == null
                    ? List.of()
                    : List.copyOf(overrideAllocationChunks);
        }
    }

    public record AllocationOverride(
            Long assigneeId,
            Long start,
            Long end,
            Long effortMillis
    ) {
    }
}
