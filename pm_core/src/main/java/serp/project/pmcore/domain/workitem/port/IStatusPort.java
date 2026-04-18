/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workitem.entity.StatusEntity;

public interface IStatusPort {
    Optional<StatusEntity> getStatusById(Long id, Long tenantId);

    Optional<StatusEntity> getStatusByIdIncludingSystem(Long id, Long tenantId);

    List<StatusEntity> getStatusesByTenantId(Long tenantId);

    Optional<StatusEntity> getStatusByStatusKey(Long tenantId, String statusKey);

    StatusEntity createStatus(StatusEntity status);

    List<StatusEntity> createStatuses(List<StatusEntity> statuses);

    boolean existsByCategoryId(Long categoryId, Long tenantId);
}
