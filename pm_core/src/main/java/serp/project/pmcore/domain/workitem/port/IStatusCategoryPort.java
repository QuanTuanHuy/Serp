/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;

import java.util.Optional;

import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;

public interface IStatusCategoryPort {
    Optional<StatusCategoryEntity> getStatusCategoryById(Long id, Long tenantId);

    Optional<StatusCategoryEntity> getStatusCategoryByIdIncludingSystem(Long id, Long tenantId);

    Optional<StatusCategoryEntity> getStatusCategoryByKey(Long tenantId, String key);

    Optional<StatusCategoryEntity> getStatusCategoryByKeyIncludingSystem(Long tenantId, String key);

    PageResult<StatusCategoryEntity> listStatusCategoriesIncludingSystem(Long tenantId,
                                                                         StatusCategoryListCriteria criteria);

    StatusCategoryEntity createStatusCategory(StatusCategoryEntity statusCategory);

    void updateStatusCategory(StatusCategoryEntity statusCategory);

    boolean existsByKey(Long tenantId, String key);
}
