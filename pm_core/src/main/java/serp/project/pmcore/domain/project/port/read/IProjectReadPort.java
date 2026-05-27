/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port.read;

import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IProjectReadPort {
    Optional<ProjectEntity> getProjectById(Long id, Long tenantId);

    Optional<ProjectEntity> getProjectByKey(String key, Long tenantId);

    boolean existsByKeyAndTenantId(String key, Long tenantId);

    boolean existsActiveProjectByCategoryId(Long categoryId, Long tenantId);

    boolean existsActiveProjectByIssueTypeSchemeId(Long issueTypeSchemeId, Long tenantId);

    List<Long> getActiveProjectIdsByIssueTypeSchemeId(Long issueTypeSchemeId, Long tenantId);

    List<ProjectEntity> getActiveProjectsByIssueTypeSchemeIds(List<Long> issueTypeSchemeIds, Long tenantId);

    boolean existsActiveProjectByPrioritySchemeId(Long prioritySchemeId, Long tenantId);

    List<Long> getActiveProjectIdsByPrioritySchemeId(Long prioritySchemeId, Long tenantId);

    List<ProjectEntity> getActiveProjectsByPrioritySchemeIds(List<Long> prioritySchemeIds, Long tenantId);

    boolean existsActiveProjectByWorkflowSchemeId(Long workflowSchemeId, Long tenantId);

    List<Long> getActiveProjectIdsByWorkflowSchemeId(Long workflowSchemeId, Long tenantId);

    List<ProjectEntity> getActiveProjectsByWorkflowSchemeIds(List<Long> workflowSchemeIds, Long tenantId);

    PageResult<ProjectEntity> getProjects(Long tenantId, Long userId, Set<String> groupKeys, String search,
                                          Long categoryId, String projectTypeKey,
                                          Boolean archived, int page, int size,
                                          String sortBy, String sortDirection);
}
