/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import java.util.List;

import serp.project.pmcore.domain.project.dto.ProjectCategoryUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.query.ProjectCategoryListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IProjectCategoryService {
    ProjectCategoryEntity createCategory(ProjectCategoryEntity category, Long tenantId, Long userId);

    ProjectCategoryEntity getCategoryById(Long categoryId, Long tenantId);

    List<ProjectCategoryEntity> getCategoriesByIds(List<Long> categoryIds);

    PageResult<ProjectCategoryEntity> listCategories(Long tenantId, ProjectCategoryListCriteria criteria);

    ProjectCategoryEntity updateCategory(Long categoryId, ProjectCategoryUpdateData data, Long tenantId, Long userId);

    ProjectCategoryEntity deleteCategory(Long categoryId, Long tenantId, Long userId);
}
