/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.query.StatusListCriteria;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.workitem.entity.StatusEntity;

public interface IStatusPort {
    Optional<StatusEntity> getStatusById(Long id, Long tenantId);

    Optional<StatusEntity> getStatusByIdIncludingSystem(Long id, Long tenantId);

    List<StatusEntity> getStatusesByTenantId(Long tenantId);

    List<StatusEntity> getStatusesByTenantIdIncludingSystem(Long tenantId);

    PageResult<StatusEntity> listStatusesIncludingSystem(Long tenantId, StatusListCriteria criteria);

    Optional<StatusEntity> getStatusByStatusKey(Long tenantId, String statusKey);

    Optional<StatusEntity> getStatusByStatusKeyIncludingSystem(Long tenantId, String statusKey);

    StatusEntity createStatus(StatusEntity status);

    void updateStatus(StatusEntity status);

    List<StatusEntity> createStatuses(List<StatusEntity> statuses);

    boolean existsByCategoryId(Long categoryId, Long tenantId);

    boolean existsByStatusKey(Long tenantId, String statusKey);
}
