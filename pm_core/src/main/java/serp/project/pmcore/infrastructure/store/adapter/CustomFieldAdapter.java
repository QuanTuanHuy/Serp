/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.domain.port.store.ICustomFieldPort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomFieldAdapter implements ICustomFieldPort {

    private final ICustomFieldRepository customFieldRepository;
    private final CustomFieldMapper customFieldMapper;

    @Override
    public CustomFieldEntity createCustomField(CustomFieldEntity customField) {
        return customFieldMapper.toEntity(customFieldRepository.save(customFieldMapper.toModel(customField)));
    }

    @Override
    public Optional<CustomFieldEntity> getCustomFieldById(Long customFieldId, Long tenantId) {
        return customFieldRepository.findByIdAndTenantId(customFieldId, tenantId)
                .map(customFieldMapper::toEntity);
    }

    @Override
    public Optional<CustomFieldEntity> getCustomFieldByIdIncludingSystem(Long customFieldId, Long tenantId) {
        return customFieldRepository.findByIdAndTenantIdOrSystemTenant(customFieldId, tenantId)
                .map(customFieldMapper::toEntity);
    }

    @Override
    public Optional<CustomFieldEntity> getCustomFieldByFieldKey(Long tenantId, String fieldKey) {
        return customFieldRepository.findFirstByTenantIdAndFieldKeyOrderByIdAsc(tenantId, fieldKey)
                .map(customFieldMapper::toEntity);
    }

    @Override
    public List<CustomFieldEntity> getCustomFieldsByFieldKeysIncludingSystem(List<String> fieldKeys, Long tenantId) {
        if (fieldKeys == null || fieldKeys.isEmpty()) {
            return Collections.emptyList();
        }
        return customFieldMapper.toEntities(
                customFieldRepository.findAllByFieldKeyInAndTenantIdOrSystemTenant(fieldKeys, tenantId)
        );
    }
}
