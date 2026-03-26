/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldContextEntity;

import java.util.List;
import java.util.Optional;

public interface ICustomFieldContextPort {
    List<CustomFieldContextEntity> createCustomFieldContexts(List<CustomFieldContextEntity> contexts);

    List<CustomFieldContextEntity> getApplicableCustomFieldContexts(Long customFieldId,
                                                                    Long projectId,
                                                                    Long issueTypeId,
                                                                    Long tenantId);

    List<CustomFieldContextEntity> getCustomFieldContextsByCustomFieldIdIncludingSystem(Long customFieldId,
                                                                                         Long tenantId);

    Optional<CustomFieldContextEntity> getCustomFieldContextByName(Long customFieldId,
                                                                   String name,
                                                                   Long tenantId);
}
