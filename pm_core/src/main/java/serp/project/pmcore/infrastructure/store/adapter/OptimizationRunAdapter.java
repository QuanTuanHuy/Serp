/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunEntity;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunPort;
import serp.project.pmcore.infrastructure.store.mapper.OptimizationRunMapper;
import serp.project.pmcore.infrastructure.store.repository.IOptimizationRunRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OptimizationRunAdapter implements IOptimizationRunPort {
    private final IOptimizationRunRepository optimizationRunRepository;
    private final OptimizationRunMapper optimizationRunMapper;

    @Override
    public OptimizationRunEntity save(OptimizationRunEntity run) {
        return optimizationRunMapper.toEntity(optimizationRunRepository.save(optimizationRunMapper.toModel(run)));
    }

    @Override
    public Optional<OptimizationRunEntity> getById(Long tenantId, Long runId) {
        return optimizationRunRepository.findByIdAndTenantId(runId, tenantId)
                .map(optimizationRunMapper::toEntity);
    }
}
