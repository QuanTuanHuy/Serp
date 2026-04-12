/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.customfield.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.customfield.port.ICustomFieldContextPort;
import serp.project.pmcore.infrastructure.store.mapper.CustomFieldContextMapper;
import serp.project.pmcore.infrastructure.store.repository.ICustomFieldContextRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomFieldContextAdapter implements ICustomFieldContextPort {

    private final ICustomFieldContextRepository customFieldContextRepository;
    private final CustomFieldContextMapper customFieldContextMapper;

    @Override
    public List<CustomFieldContextEntity> getApplicableCustomFieldContexts(Long customFieldId,
                                                                           String issueTypeKey) {
        return customFieldContextMapper.toEntities(
                customFieldContextRepository.findApplicableContexts(customFieldId, issueTypeKey)
        );
    }
}
