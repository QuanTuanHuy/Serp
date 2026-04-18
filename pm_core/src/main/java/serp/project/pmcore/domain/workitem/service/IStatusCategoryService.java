/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.service;

import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.workitem.dto.StatusCategoryUpdateData;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.query.StatusCategoryListCriteria;

public interface IStatusCategoryService {
    StatusCategoryEntity createStatusCategory(StatusCategoryEntity statusCategory, Long tenantId, Long userId);

    StatusCategoryEntity getStatusCategoryById(Long statusCategoryId, Long tenantId);

    StatusCategoryEntity getVisibleStatusCategoryById(Long statusCategoryId, Long tenantId);

    PageResult<StatusCategoryEntity> listVisibleStatusCategories(Long tenantId, StatusCategoryListCriteria criteria);

    StatusCategoryEntity updateStatusCategory(Long statusCategoryId,
                                              StatusCategoryUpdateData data,
                                              Long tenantId,
                                              Long userId);

    StatusCategoryEntity deleteStatusCategory(Long statusCategoryId, Long tenantId, Long userId);
}
