/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.CustomFieldContextIssueTypeEntity;

import java.util.List;

public interface ICustomFieldContextIssueTypePort {
    List<CustomFieldContextIssueTypeEntity> createCustomFieldContextIssueTypes(List<CustomFieldContextIssueTypeEntity> contextIssueTypes);

    List<CustomFieldContextIssueTypeEntity> getCustomFieldContextIssueTypesByContextId(Long contextId, Long tenantId);

    List<CustomFieldContextIssueTypeEntity> getCustomFieldContextIssueTypesByContextIdIncludingSystem(Long contextId,
                                                                                                      Long tenantId);
}
