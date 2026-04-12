/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.entity.TenantSchemeDefaultEntity;
import serp.project.pmcore.domain.shared.port.store.ITenantSchemeDefaultPort;
import serp.project.pmcore.infrastructure.store.mapper.TenantSchemeDefaultMapper;
import serp.project.pmcore.infrastructure.store.repository.ITenantSchemeDefaultRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TenantSchemeDefaultAdapter implements ITenantSchemeDefaultPort {

    private final ITenantSchemeDefaultRepository tenantSchemeDefaultRepository;
    private final TenantSchemeDefaultMapper tenantSchemeDefaultMapper;

    @Override
    public List<TenantSchemeDefaultEntity> getDefaultsByTenantId(Long tenantId) {
        return tenantSchemeDefaultMapper.toEntities(
                tenantSchemeDefaultRepository.findAllByTenantId(tenantId)
        );
    }

    @Override
    public List<TenantSchemeDefaultEntity> getDefaultsByTenantIdIncludingSystem(Long tenantId) {
        return tenantSchemeDefaultMapper.toEntities(
                tenantSchemeDefaultRepository.findAllByTenantIdIncludingSystem(tenantId)
        );
    }
}
