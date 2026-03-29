/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.entity.TenantSchemeMappingEntity;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.infrastructure.store.mapper.TenantSchemeMappingMapper;
import serp.project.pmcore.infrastructure.store.repository.ITenantSchemeMappingRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TenantSchemeMappingAdapter implements ITenantSchemeMappingPort {

    private final ITenantSchemeMappingRepository tenantSchemeMappingRepository;
    private final TenantSchemeMappingMapper tenantSchemeMappingMapper;

    @Override
    public Optional<TenantSchemeMappingEntity> getMapping(Long tenantId, SchemeType schemeType, Long sourceSchemeId) {
        return tenantSchemeMappingRepository
                .findByTenantIdAndSchemeTypeAndSourceSchemeId(tenantId, schemeType, sourceSchemeId)
                .map(tenantSchemeMappingMapper::toEntity);
    }

    @Override
    public TenantSchemeMappingEntity saveMapping(TenantSchemeMappingEntity mapping) {
        return tenantSchemeMappingMapper.toEntity(
                tenantSchemeMappingRepository.save(tenantSchemeMappingMapper.toModel(mapping))
        );
    }
}
