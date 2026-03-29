/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import serp.project.pmcore.domain.project.entity.ProjectEntity;

public interface IProjectService {

    ProjectEntity createProject(ProjectEntity project, Long tenantId, Long userId);

    ProjectEntity saveProject(ProjectEntity entity, Long userId);

    ProjectEntity updateProject(Long projectId, ProjectEntity updateData, Long tenantId, Long userId);

    ProjectEntity getProjectById(Long id, Long tenantId);

    ProjectEntity getProjectByKey(String key, Long tenantId);

    void deleteProject(Long id, Long tenantId);

    ProjectEntity archiveProject(Long id, Long tenantId, Long userId);

    ProjectEntity unarchiveProject(Long id, Long tenantId, Long userId);
}
