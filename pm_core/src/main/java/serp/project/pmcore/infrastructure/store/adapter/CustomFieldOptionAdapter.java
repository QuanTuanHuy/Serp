/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.port.store.ICustomFieldOptionPort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldOptionMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldOptionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomFieldOptionAdapter implements ICustomFieldOptionPort {

    private final ICustomFieldOptionRepository customFieldOptionRepository;
    private final CustomFieldOptionMapper customFieldOptionMapper;

    @Override
    public List<CustomFieldOptionEntity> createCustomFieldOptions(List<CustomFieldOptionEntity> options) {
        return customFieldOptionMapper.toEntities(
                customFieldOptionRepository.saveAll(customFieldOptionMapper.toModels(options))
        );
    }

    @Override
    public List<CustomFieldOptionEntity> getCustomFieldOptionsByContextId(Long contextId, Long tenantId) {
        return customFieldOptionMapper.toEntities(
                customFieldOptionRepository.findAllByContextIdAndTenantId(contextId, tenantId)
        );
    }

    @Override
    public List<CustomFieldOptionEntity> getCustomFieldOptionsByContextIdIncludingSystem(Long contextId, Long tenantId) {
        return customFieldOptionMapper.toEntities(
                customFieldOptionRepository.findAllByContextIdAndTenantIdOrSystemTenant(contextId, tenantId)
        );
    }
}
