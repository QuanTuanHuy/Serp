/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;

public interface IPrioritySchemeService {

    PrioritySchemeEntity getPrioritySchemeById(Long prioritySchemeId, Long tenantId);

    Long resolveDefaultPriorityId(Long prioritySchemeId, Long tenantId);

    Long validatePriorityIdInScheme(Long prioritySchemeId, Long requestedPriorityId, Long tenantId);
}
