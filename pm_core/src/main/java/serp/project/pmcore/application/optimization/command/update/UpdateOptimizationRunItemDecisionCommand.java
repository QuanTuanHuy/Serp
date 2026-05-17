/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.update;

import serp.project.pmcore.application.shared.cqrs.command.ICommand;
import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.domain.optimization.enums.OptimizationDecision;

public record UpdateOptimizationRunItemDecisionCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        Long runId,
        Long workItemId,
        OptimizationDecision assignmentDecision,
        OptimizationDecision scheduleDecision,
        Long overrideAssigneeId,
        Long overridePlannedStart,
        Long overridePlannedEnd
) implements ICommand<OptimizationRunReviewView> {
}
