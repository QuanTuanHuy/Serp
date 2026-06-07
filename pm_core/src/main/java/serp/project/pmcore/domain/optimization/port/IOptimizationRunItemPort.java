/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;

import java.util.List;

public interface IOptimizationRunItemPort {
    List<OptimizationRunItemEntity> saveAll(List<OptimizationRunItemEntity> items);

    OptimizationRunItemEntity save(OptimizationRunItemEntity item);

    List<OptimizationRunItemEntity> listByRunId(Long tenantId, Long runId);
}
