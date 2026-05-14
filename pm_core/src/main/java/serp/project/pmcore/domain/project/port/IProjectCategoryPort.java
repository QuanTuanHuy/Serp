/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port;

import serp.project.pmcore.domain.project.query.ProjectCategoryListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;

public interface IProjectCategoryPort {
    ProjectCategoryEntity createCategory(ProjectCategoryEntity category);

    Optional<ProjectCategoryEntity> getCategoryById(Long id, Long tenantId);

    List<ProjectCategoryEntity> getCategoriesByIds(List<Long> categoryIds);

    Optional<ProjectCategoryEntity> getCategoryByIdIncludingSystem(Long id, Long tenantId);

    PageResult<ProjectCategoryEntity> listCategories(Long tenantId, ProjectCategoryListCriteria criteria);

    void updateCategory(ProjectCategoryEntity category);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
