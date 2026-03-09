/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.pmcore.core.domain.entity.ProjectEntity;

import java.util.List;
import java.util.Optional;

public interface IProjectPort {
    ProjectEntity saveProject(ProjectEntity project);

    Optional<ProjectEntity> getProjectById(Long id, Long tenantId);

    Optional<ProjectEntity> getProjectByKey(String key, Long tenantId);

    boolean existsByKeyAndTenantId(String key, Long tenantId);

    Pair<List<ProjectEntity>, Long> getProjects(Long tenantId, String search,
                                                 Long categoryId, String projectTypeKey,
                                                 Boolean archived, int page, int size,
                                                 String sortBy, String sortDirection);

    void deleteProjectById(Long id, Long tenantId);
}
