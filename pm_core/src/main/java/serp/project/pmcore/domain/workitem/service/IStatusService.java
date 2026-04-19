/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.StatusUpdateData;
import java.util.List;

import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;

public interface IStatusService {
    StatusEntity createStatus(StatusEntity status, Long tenantId, Long userId);

    List<StatusEntity> getStatusesByTenantId(Long tenantId);

    PageResult<StatusEntity> listVisibleStatuses(Long tenantId, StatusListCriteria criteria);

    List<StatusEntity> createStatuses(List<StatusEntity> statuses, Long tenantId, Long userId);

    StatusEntity getStatusById(Long id, Long tenantId);

    StatusEntity getVisibleStatusById(Long id, Long tenantId);

    StatusEntity updateStatus(Long id, StatusUpdateData data, Long tenantId, Long userId);

    StatusEntity deleteStatus(Long id, Long tenantId, Long userId);

    StatusCategoryEntity getStatusCategoryById(Long id, Long tenantId);

    StatusCategoryEntity getStatusCategoryByIdIncludingSystem(Long id, Long tenantId);
}
