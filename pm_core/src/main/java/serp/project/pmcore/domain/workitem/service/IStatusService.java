/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.domain.workitem.service;

import java.util.List;

import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;

public interface IStatusService {
    List<StatusEntity> getStatusesByTenantId(Long tenantId);

    List<StatusEntity> createStatuses(List<StatusEntity> statuses, Long tenantId, Long userId);

    StatusEntity getStatusById(Long id, Long tenantId);

    StatusCategoryEntity getStatusCategoryById(Long id, Long tenantId);

    StatusCategoryEntity getStatusCategoryByIdIncludingSystem(Long id, Long tenantId);
}
