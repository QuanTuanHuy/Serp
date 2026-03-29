/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service;

import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;

public interface IPrioritySchemeService {

    PrioritySchemeEntity getPrioritySchemeById(Long prioritySchemeId, Long tenantId);

    Long resolvePriorityId(Long prioritySchemeId, Long tenantId);
}
