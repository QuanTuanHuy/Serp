/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldContextProjectEntity;

import java.util.List;

public interface ICustomFieldContextProjectPort {
    List<CustomFieldContextProjectEntity> createCustomFieldContextProjects(List<CustomFieldContextProjectEntity> contextProjects);

    List<CustomFieldContextProjectEntity> getCustomFieldContextProjectsByContextId(Long contextId, Long tenantId);

    List<CustomFieldContextProjectEntity> getCustomFieldContextProjectsByContextIdIncludingSystem(Long contextId,
                                                                                                  Long tenantId);
}
