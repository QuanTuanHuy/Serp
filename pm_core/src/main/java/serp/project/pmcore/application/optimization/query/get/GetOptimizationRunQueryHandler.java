/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GetOptimizationRunQueryHandler implements IQueryHandler<GetOptimizationRunQuery, OptimizationRunReviewView> {
    private final IOptimizationRunPort optimizationRunPort;
    private final IOptimizationRunItemPort optimizationRunItemPort;
    private final IOptimizationRunWarningPort optimizationRunWarningPort;
    private final OptimizationRunReviewAssembler optimizationRunReviewAssembler;

    @Override
    @Transactional(readOnly = true)
    public OptimizationRunReviewView handle(GetOptimizationRunQuery query) {
        OptimizationRunEntity run = optimizationRunPort.getById(query.tenantId(), query.runId())
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                        "Optimization run not found: id=" + query.runId()));
        if (!Objects.equals(run.getProjectId(), query.projectId())) {
            throw new ResourceNotFoundException(DomainErrorCode.NOT_FOUND,
                    "Optimization run not found in project: id=" + query.runId());
        }
        return optimizationRunReviewAssembler.toView(
                run,
                optimizationRunItemPort.listByRunId(query.tenantId(), query.runId()),
                optimizationRunWarningPort.listByRunId(query.tenantId(), query.runId())
        );
    }
}
