/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service;

import serp.project.pmcore.domain.priority.dto.PrioritySchemeUpdateData;
import serp.project.pmcore.domain.priority.entity.PrioritySchemeEntity;
import serp.project.pmcore.domain.priority.query.PrioritySchemeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;

public interface IPrioritySchemeService {

    PrioritySchemeEntity createPriorityScheme(PrioritySchemeEntity scheme, Long tenantId, Long userId);

    PrioritySchemeEntity getPrioritySchemeById(Long prioritySchemeId, Long tenantId);

    PrioritySchemeEntity getVisiblePrioritySchemeById(Long prioritySchemeId, Long tenantId);

    PrioritySchemeEntity getVisiblePrioritySchemeDetailById(Long prioritySchemeId, Long tenantId);

    PageResult<PrioritySchemeEntity> listVisiblePrioritySchemes(Long tenantId, PrioritySchemeListCriteria criteria);

    PrioritySchemeEntity updatePriorityScheme(Long prioritySchemeId, PrioritySchemeUpdateData data, Long tenantId, Long userId);

    PrioritySchemeEntity deletePriorityScheme(Long prioritySchemeId, Long tenantId, Long userId);

    PrioritySchemeEntity replacePrioritySchemeItems(Long prioritySchemeId, List<Long> priorityIds, Long tenantId, Long userId);

    Long resolveDefaultPriorityId(Long prioritySchemeId, Long tenantId);

    Long validatePriorityIdInScheme(Long prioritySchemeId, Long requestedPriorityId, Long tenantId);
}
