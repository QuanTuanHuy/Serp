/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port;

import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;

public interface IProjectRolePort {
    ProjectRoleEntity saveProjectRole(ProjectRoleEntity role);

    Optional<ProjectRoleEntity> getProjectRoleById(Long roleId, Long tenantId);

    Optional<ProjectRoleEntity> getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId);

    Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String name, Long tenantId);

    List<ProjectRoleEntity> getProjectRolesByNameIncludingSystem(String name, Long tenantId);

    List<ProjectRoleEntity> getProjectRolesIncludingSystem(Long tenantId);

    PageResult<ProjectRoleEntity> getProjectRolesIncludingSystem(Long tenantId, ProjectRoleListCriteria criteria);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
