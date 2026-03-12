/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.StatusCategoryEntity;

import java.util.Optional;

public interface IStatusCategoryPort {
    Optional<StatusCategoryEntity> getStatusCategoryById(Long id, Long tenantId);

    Optional<StatusCategoryEntity> getStatusCategoryByIdIncludingSystem(Long id, Long tenantId);

    Optional<StatusCategoryEntity> getStatusCategoryByKey(Long tenantId, String key);

    StatusCategoryEntity createStatusCategory(StatusCategoryEntity statusCategory);
}
