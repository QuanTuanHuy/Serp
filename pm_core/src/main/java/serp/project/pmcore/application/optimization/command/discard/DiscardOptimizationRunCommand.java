/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.command.discard;

import serp.project.pmcore.application.optimization.query.get.OptimizationRunReviewView;
import serp.project.pmcore.application.shared.cqrs.command.ICommand;

public record DiscardOptimizationRunCommand(
        Long tenantId,
        Long userId,
        Long projectId,
        Long runId
) implements ICommand<OptimizationRunReviewView> {
}
