/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.optimization.support;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.enums.OptimizationRunStatus;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.Objects;
import java.util.Set;

@Service
public class OptimizationRunGuard {
    private final IOptimizationRunPort optimizationRunPort;

    public OptimizationRunGuard(IOptimizationRunPort optimizationRunPort) {
        this.optimizationRunPort = optimizationRunPort;
    }

    public OptimizationRunEntity requireRunInProject(Long tenantId, Long projectId, Long runId) {
        OptimizationRunEntity run = optimizationRunPort.getById(tenantId, runId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.NOT_FOUND,
                        "Optimization run not found: id=" + runId
                ));
        if (!Objects.equals(run.getProjectId(), projectId)) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.NOT_FOUND,
                    "Optimization run not found for project: runId=" + runId + ", projectId=" + projectId
            );
        }
        return run;
    }

    public void ensureStatus(OptimizationRunEntity run, Set<OptimizationRunStatus> allowedStatuses, String action) {
        if (!allowedStatuses.contains(run.getStatus())) {
            throw new IllegalArgumentException("Optimization run cannot be " + action + " in status " + run.getStatus());
        }
    }
}
