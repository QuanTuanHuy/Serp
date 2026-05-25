/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.port;

import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;

import java.util.List;

public interface IOptimizationRunWarningPort {
    List<OptimizationRunWarningEntity> saveAll(List<OptimizationRunWarningEntity> warnings);

    List<OptimizationRunWarningEntity> listByRunId(Long tenantId, Long runId);
}
