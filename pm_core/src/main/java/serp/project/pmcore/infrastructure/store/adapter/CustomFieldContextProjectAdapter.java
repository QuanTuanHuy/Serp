/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextProjectEntity;
import serp.project.pmcore.domain.port.store.ICustomFieldContextProjectPort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldContextProjectMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldContextProjectRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomFieldContextProjectAdapter implements ICustomFieldContextProjectPort {

    private final ICustomFieldContextProjectRepository customFieldContextProjectRepository;
    private final CustomFieldContextProjectMapper customFieldContextProjectMapper;

    @Override
    public List<CustomFieldContextProjectEntity> createCustomFieldContextProjects(List<CustomFieldContextProjectEntity> contextProjects) {
        return customFieldContextProjectMapper.toEntities(
                customFieldContextProjectRepository.saveAll(customFieldContextProjectMapper.toModels(contextProjects))
        );
    }

    @Override
    public List<CustomFieldContextProjectEntity> getCustomFieldContextProjectsByContextId(Long contextId, Long tenantId) {
        return customFieldContextProjectMapper.toEntities(
                customFieldContextProjectRepository.findAllByContextIdAndTenantId(contextId, tenantId)
        );
    }

    @Override
    public List<CustomFieldContextProjectEntity> getCustomFieldContextProjectsByContextIdIncludingSystem(Long contextId,
                                                                                                          Long tenantId) {
        return customFieldContextProjectMapper.toEntities(
                customFieldContextProjectRepository.findAllByContextIdAndTenantIdOrSystemTenant(contextId, tenantId)
        );
    }
}
