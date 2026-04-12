/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldOptionPort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldOptionMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldOptionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomFieldOptionAdapter implements ICustomFieldOptionPort {

    private final ICustomFieldOptionRepository customFieldOptionRepository;
    private final CustomFieldOptionMapper customFieldOptionMapper;

    @Override
    public List<CustomFieldOptionEntity> getCustomFieldOptionsByContextId(Long contextId) {
        return customFieldOptionMapper.toEntities(
                customFieldOptionRepository.findAllByContextId(contextId)
        );
    }
}
