/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port;

import java.util.Optional;

import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;

public interface IProjectCategoryPort {
    Optional<ProjectCategoryEntity> getCategoryById(Long id, Long tenantId);

    Optional<ProjectCategoryEntity> getCategoryByIdIncludingSystem(Long id, Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
