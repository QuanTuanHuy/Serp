/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import org.springframework.data.util.Pair;
import serp.project.pmcore.core.domain.dto.request.GetProjectParams;
import serp.project.pmcore.core.domain.entity.ProjectEntity;

import java.util.List;

public interface IProjectService {

    ProjectEntity createProject(ProjectEntity project, Long tenantId, Long userId);

    ProjectEntity saveProject(ProjectEntity entity, Long userId);

    ProjectEntity updateProject(Long projectId, ProjectEntity updateData, Long tenantId, Long userId);

    ProjectEntity getProjectById(Long id, Long tenantId);

    ProjectEntity getProjectByKey(String key, Long tenantId);

    Pair<List<ProjectEntity>, Long> getProjects(Long tenantId, GetProjectParams params);

    void deleteProject(Long id, Long tenantId);

    ProjectEntity archiveProject(Long id, Long tenantId, Long userId);

    ProjectEntity unarchiveProject(Long id, Long tenantId, Long userId);

    void validateKeyFormat(String key);

    void validateKeyUniqueness(String key, Long tenantId);

    void validateCategoryExists(Long categoryId, Long tenantId);
}
