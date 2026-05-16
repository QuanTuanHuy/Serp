/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;

import java.util.Optional;

public interface IOptimizationRunPort {
    OptimizationRunEntity save(OptimizationRunEntity run);

    Optional<OptimizationRunEntity> getById(Long tenantId, Long runId);
}
