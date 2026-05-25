/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.optimization.support.OptimizationRunGuard;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;

@Service
@RequiredArgsConstructor
public class GetOptimizationRunQueryHandler implements IQueryHandler<GetOptimizationRunQuery, OptimizationRunReviewView> {
    private final OptimizationRunGuard optimizationRunGuard;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;

    @Override
    @Transactional(readOnly = true)
    public OptimizationRunReviewView handle(GetOptimizationRunQuery query) {
        OptimizationRunEntity run = optimizationRunGuard.requireRunInProject(
                query.tenantId(),
                query.projectId(),
                query.runId()
        );
        return optimizationRunReviewAssembler.toView(
                run,
                optimizationRunItemPort.listByRunId(query.tenantId(), query.runId()),
                optimizationRunWarningPort.listByRunId(query.tenantId(), query.runId())
        );
    }
}
