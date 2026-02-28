/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import serp.project.pmcore.core.domain.entity.PrioritySchemeEntity;

public interface IPrioritySchemeService {

    PrioritySchemeEntity getPrioritySchemeById(Long prioritySchemeId, Long tenantId);

    Long resolvePriorityId(Long prioritySchemeId, Long tenantId);
}
