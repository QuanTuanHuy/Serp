/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldContextDefaultValueEntity;

import java.util.List;

public interface ICustomFieldContextDefaultValuePort {
    List<CustomFieldContextDefaultValueEntity> getCustomFieldContextDefaultValuesByContextId(Long contextId,
                                                                                              Long tenantId);
}
