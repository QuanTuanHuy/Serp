/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunWarningEntity;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunWarningPort;
import serp.project.pmcore.infrastructure.store.mapper.OptimizationRunWarningMapper;
import serp.project.pmcore.infrastructure.store.repository.IOptimizationRunWarningRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OptimizationRunWarningAdapter implements IOptimizationRunWarningPort {
    private final IOptimizationRunWarningRepository optimizationRunWarningRepository;
    private final OptimizationRunWarningMapper optimizationRunWarningMapper;

    @Override
    public List<OptimizationRunWarningEntity> saveAll(List<OptimizationRunWarningEntity> warnings) {
        return optimizationRunWarningMapper.toEntities(
                optimizationRunWarningRepository.saveAll(optimizationRunWarningMapper.toModels(warnings))
        );
    }

    @Override
    public List<OptimizationRunWarningEntity> listByRunId(Long tenantId, Long runId) {
        return optimizationRunWarningMapper.toEntities(optimizationRunWarningRepository.findAllByTenantIdAndRunId(tenantId, runId));
    }
}
