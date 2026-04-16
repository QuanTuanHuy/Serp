/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port.read;

import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.Optional;

public interface IProjectReadPort {
    Optional<ProjectEntity> getProjectById(Long id, Long tenantId);

    Optional<ProjectEntity> getProjectByKey(String key, Long tenantId);

    boolean existsByKeyAndTenantId(String key, Long tenantId);

    boolean existsActiveProjectByCategoryId(Long categoryId, Long tenantId);

    PageResult<ProjectEntity> getProjects(Long tenantId, String search,
                                          Long categoryId, String projectTypeKey,
                                          Boolean archived, int page, int size,
                                          String sortBy, String sortDirection);
}
