/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.ProjectCategoryEntity;

import java.util.Optional;

public interface IProjectCategoryPort {
    Optional<ProjectCategoryEntity> getCategoryById(Long id, Long tenantId);

    Optional<ProjectCategoryEntity> getCategoryByIdIncludingSystem(Long id, Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
