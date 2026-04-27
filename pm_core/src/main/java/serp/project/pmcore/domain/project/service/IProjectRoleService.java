/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import serp.project.pmcore.domain.project.dto.ProjectRoleUpdateData;
import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IProjectRoleService {
    ProjectRoleEntity createProjectRole(ProjectRoleEntity role, Long tenantId, Long userId);

    ProjectRoleEntity getProjectRoleById(Long roleId, Long tenantId);

    ProjectRoleEntity getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId);

    Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String roleName, Long tenantId);

    List<ProjectRoleEntity> getProjectRolesByNameIncludingSystem(String roleName, Long tenantId);

    PageResult<ProjectRoleEntity> listVisibleProjectRoles(Long tenantId, ProjectRoleListCriteria criteria);

    ProjectRoleEntity updateProjectRole(Long roleId, ProjectRoleUpdateData data, Long tenantId, Long userId);

    ProjectRoleEntity deleteProjectRole(Long roleId, Long tenantId, Long userId);
}
