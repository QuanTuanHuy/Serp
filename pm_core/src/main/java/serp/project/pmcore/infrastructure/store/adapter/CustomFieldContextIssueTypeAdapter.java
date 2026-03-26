/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldContextIssueTypeEntity;
import serp.project.pmcore.domain.port.store.ICustomFieldContextIssueTypePort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldContextIssueTypeMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldContextIssueTypeRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomFieldContextIssueTypeAdapter implements ICustomFieldContextIssueTypePort {

    private final ICustomFieldContextIssueTypeRepository customFieldContextIssueTypeRepository;
    private final CustomFieldContextIssueTypeMapper customFieldContextIssueTypeMapper;

    @Override
    public List<CustomFieldContextIssueTypeEntity> createCustomFieldContextIssueTypes(List<CustomFieldContextIssueTypeEntity> contextIssueTypes) {
        return customFieldContextIssueTypeMapper.toEntities(
                customFieldContextIssueTypeRepository.saveAll(
                        customFieldContextIssueTypeMapper.toModels(contextIssueTypes)
                )
        );
    }

    @Override
    public List<CustomFieldContextIssueTypeEntity> getCustomFieldContextIssueTypesByContextId(Long contextId,
                                                                                              Long tenantId) {
        return customFieldContextIssueTypeMapper.toEntities(
                customFieldContextIssueTypeRepository.findAllByContextIdAndTenantId(contextId, tenantId)
        );
    }

    @Override
    public List<CustomFieldContextIssueTypeEntity> getCustomFieldContextIssueTypesByContextIdIncludingSystem(Long contextId,
                                                                                                              Long tenantId) {
        return customFieldContextIssueTypeMapper.toEntities(
                customFieldContextIssueTypeRepository.findAllByContextIdAndTenantIdOrSystemTenant(contextId, tenantId)
        );
    }
}
