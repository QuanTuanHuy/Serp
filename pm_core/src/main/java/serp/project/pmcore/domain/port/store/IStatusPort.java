/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.StatusEntity;

import java.util.List;
import java.util.Optional;

public interface IStatusPort {
    Optional<StatusEntity> getStatusById(Long id, Long tenantId);

    Optional<StatusEntity> getStatusByIdIncludingSystem(Long id, Long tenantId);

    List<StatusEntity> getStatusesByTenantId(Long tenantId);

    Optional<StatusEntity> getStatusByStatusKey(Long tenantId, String statusKey);

    StatusEntity createStatus(StatusEntity status);

    List<StatusEntity> createStatuses(List<StatusEntity> statuses);
}
