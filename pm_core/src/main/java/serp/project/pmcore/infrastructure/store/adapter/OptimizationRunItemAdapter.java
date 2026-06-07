/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.optimization.entity.OptimizationRunItemEntity;
import serp.project.pmcore.domain.optimization.port.IOptimizationRunItemPort;
import serp.project.pmcore.infrastructure.store.mapper.OptimizationRunItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IOptimizationRunItemRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OptimizationRunItemAdapter implements IOptimizationRunItemPort {
    private final IOptimizationRunItemRepository optimizationRunItemRepository;
    private final OptimizationRunItemMapper optimizationRunItemMapper;

    @Override
    public List<OptimizationRunItemEntity> saveAll(List<OptimizationRunItemEntity> items) {
        return optimizationRunItemMapper.toEntities(
                optimizationRunItemRepository.saveAll(optimizationRunItemMapper.toModels(items))
        );
    }

    @Override
    public OptimizationRunItemEntity save(OptimizationRunItemEntity item) {
        return optimizationRunItemMapper.toEntity(optimizationRunItemRepository.save(optimizationRunItemMapper.toModel(item)));
    }

    @Override
    public List<OptimizationRunItemEntity> listByRunId(Long tenantId, Long runId) {
        return optimizationRunItemMapper.toEntities(optimizationRunItemRepository.findAllByTenantIdAndRunId(tenantId, runId));
    }
}
