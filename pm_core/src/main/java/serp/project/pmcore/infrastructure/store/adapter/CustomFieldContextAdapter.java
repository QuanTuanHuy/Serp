/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.port.store.ICustomFieldContextPort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldContextMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldContextRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomFieldContextAdapter implements ICustomFieldContextPort {

    private final ICustomFieldContextRepository customFieldContextRepository;
    private final CustomFieldContextMapper customFieldContextMapper;

    @Override
    public List<CustomFieldContextEntity> createCustomFieldContexts(List<CustomFieldContextEntity> contexts) {
        return customFieldContextMapper.toEntities(
                customFieldContextRepository.saveAll(customFieldContextMapper.toModels(contexts))
        );
    }

    @Override
    public List<CustomFieldContextEntity> getApplicableCustomFieldContexts(Long customFieldId,
                                                                           Long projectId,
                                                                           Long issueTypeId,
                                                                           Long tenantId) {
        return customFieldContextMapper.toEntities(
                customFieldContextRepository.findApplicableContexts(customFieldId, projectId, issueTypeId, tenantId)
        );
    }

    @Override
    public List<CustomFieldContextEntity> getCustomFieldContextsByCustomFieldIdIncludingSystem(Long customFieldId,
                                                                                                Long tenantId) {
        return customFieldContextMapper.toEntities(
                customFieldContextRepository.findAllByCustomFieldIdAndTenantIdOrSystemTenant(customFieldId, tenantId)
        );
    }

    @Override
    public Optional<CustomFieldContextEntity> getCustomFieldContextByName(Long customFieldId,
                                                                          String name,
                                                                          Long tenantId) {
        return customFieldContextRepository.findFirstByTenantIdAndCustomFieldIdAndNameOrderByIdAsc(
                        tenantId,
                        customFieldId,
                        name
                )
                .map(customFieldContextMapper::toEntity);
    }
}
