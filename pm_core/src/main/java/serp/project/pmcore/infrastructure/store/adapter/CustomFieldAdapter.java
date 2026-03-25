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

@Component
@RequiredArgsConstructor
public class CustomFieldAdapter implements ICustomFieldPort {

    private final ICustomFieldRepository customFieldRepository;
    private final CustomFieldMapper customFieldMapper;

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
