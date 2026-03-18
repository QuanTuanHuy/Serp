/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.TenantWorkflowMappingEntity;
import serp.project.pmcore.domain.port.store.ITenantWorkflowMappingPort;
import serp.project.pmcore.infrastructure.store.mapper.TenantWorkflowMappingMapper;
import serp.project.pmcore.infrastructure.store.repository.ITenantWorkflowMappingRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantWorkflowMappingAdapter implements ITenantWorkflowMappingPort {

    private final ITenantWorkflowMappingRepository tenantWorkflowMappingRepository;
    private final TenantWorkflowMappingMapper tenantWorkflowMappingMapper;

    @Override
    public Optional<TenantWorkflowMappingEntity> getMapping(Long tenantId, Long sourceWorkflowId) {
        return tenantWorkflowMappingRepository
                .findByTenantIdAndSourceWorkflowId(tenantId, sourceWorkflowId)
                .map(tenantWorkflowMappingMapper::toEntity);
    }

    @Override
    public TenantWorkflowMappingEntity saveMapping(TenantWorkflowMappingEntity mapping) {
        return tenantWorkflowMappingMapper.toEntity(
                tenantWorkflowMappingRepository.save(tenantWorkflowMappingMapper.toModel(mapping))
        );
    }
}
