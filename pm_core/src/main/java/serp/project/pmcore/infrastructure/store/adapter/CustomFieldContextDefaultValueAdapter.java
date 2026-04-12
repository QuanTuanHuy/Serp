/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextDefaultValuePort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldContextDefaultValueMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldContextDefaultValueRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomFieldContextDefaultValueAdapter implements ICustomFieldContextDefaultValuePort {

    private final ICustomFieldContextDefaultValueRepository customFieldContextDefaultValueRepository;
    private final CustomFieldContextDefaultValueMapper customFieldContextDefaultValueMapper;

    @Override
    public List<CustomFieldContextDefaultValueEntity> getCustomFieldContextDefaultValuesByContextId(Long contextId) {
        return customFieldContextDefaultValueMapper.toEntities(
                customFieldContextDefaultValueRepository.findAllByContextId(contextId)
        );
    }
}
