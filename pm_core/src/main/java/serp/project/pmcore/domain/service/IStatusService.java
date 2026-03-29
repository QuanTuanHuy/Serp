/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.domain.service;

import java.util.List;

import serp.project.pmcore.domain.workitem.entity.StatusEntity;

public interface IStatusService {
    List<StatusEntity> getStatusesByTenantId(Long tenantId);

    List<StatusEntity> createStatuses(List<StatusEntity> statuses, Long tenantId, Long userId);
}
