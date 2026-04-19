/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.priority.service;

import java.util.List;

import serp.project.pmcore.domain.priority.dto.PriorityUpdateData;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.priority.query.PriorityListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IPriorityService {
    PriorityEntity createPriority(PriorityEntity priority, Long tenantId, Long userId);

    PriorityEntity getPriorityById(Long priorityId, Long tenantId);

    PriorityEntity getVisiblePriorityById(Long priorityId, Long tenantId);

    List<PriorityEntity> getVisiblePrioritiesByIds(List<Long> priorityIds, Long tenantId);

    PageResult<PriorityEntity> listVisiblePriorities(Long tenantId, PriorityListCriteria criteria);

    PriorityEntity updatePriority(Long priorityId, PriorityUpdateData data, Long tenantId, Long userId);

    PriorityEntity deletePriority(Long priorityId, Long tenantId, Long userId);
}
