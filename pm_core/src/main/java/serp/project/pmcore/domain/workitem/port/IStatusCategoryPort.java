/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import java.util.Optional;

import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;

public interface IStatusCategoryPort {
    Optional<StatusCategoryEntity> getStatusCategoryById(Long id, Long tenantId);

    Optional<StatusCategoryEntity> getStatusCategoryByIdIncludingSystem(Long id, Long tenantId);

    Optional<StatusCategoryEntity> getStatusCategoryByKey(Long tenantId, String key);

    StatusCategoryEntity createStatusCategory(StatusCategoryEntity statusCategory);
}
