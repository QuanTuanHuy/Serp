/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldEntity;

import java.util.List;
import java.util.Optional;

public interface ICustomFieldPort {
    CustomFieldEntity createCustomField(CustomFieldEntity customField);

    Optional<CustomFieldEntity> getCustomFieldById(Long customFieldId, Long tenantId);

    Optional<CustomFieldEntity> getCustomFieldByIdIncludingSystem(Long customFieldId, Long tenantId);

    Optional<CustomFieldEntity> getCustomFieldByFieldKey(Long tenantId, String fieldKey);

    List<CustomFieldEntity> getCustomFieldsByFieldKeysIncludingSystem(List<String> fieldKeys, Long tenantId);
}
