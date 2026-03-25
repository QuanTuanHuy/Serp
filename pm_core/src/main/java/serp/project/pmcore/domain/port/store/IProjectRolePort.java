/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.project.ProjectRoleEntity;

import java.util.List;
import java.util.Optional;

public interface IProjectRolePort {
    ProjectRoleEntity saveProjectRole(ProjectRoleEntity role);

    Optional<ProjectRoleEntity> getProjectRoleById(Long roleId, Long tenantId);

    Optional<ProjectRoleEntity> getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId);

    Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String name, Long tenantId);

    List<ProjectRoleEntity> getProjectRolesIncludingSystem(Long tenantId);
}
