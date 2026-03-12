/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.pmcore.core.service;

import java.util.List;

import serp.project.pmcore.core.domain.entity.StatusEntity;

public interface IStatusService {
    List<StatusEntity> getStatusesByTenantId(Long tenantId);

    List<StatusEntity> createStatuses(List<StatusEntity> statuses, Long tenantId, Long userId);
}
