/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;

import java.util.List;

public interface ICustomFieldContextDefaultValuePort {
    List<CustomFieldContextDefaultValueEntity> createCustomFieldContextDefaultValues(List<CustomFieldContextDefaultValueEntity> defaultValues);

    List<CustomFieldContextDefaultValueEntity> getCustomFieldContextDefaultValuesByContextId(Long contextId,
                                                                                              Long tenantId);

    List<CustomFieldContextDefaultValueEntity> getCustomFieldContextDefaultValuesByContextIdIncludingSystem(Long contextId,
                                                                                                             Long tenantId);
}
