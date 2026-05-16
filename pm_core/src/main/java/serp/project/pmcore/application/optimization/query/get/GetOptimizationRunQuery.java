/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

public record GetOptimizationRunQuery(
        Long tenantId,
        Long projectId,
        Long runId
) implements IQuery<OptimizationRunReviewView> {
}
