/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.entity.project.ProjectRoleEntity;

import java.util.Optional;

public interface IProjectRoleService {
    ProjectRoleEntity getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId);

    Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String roleName, Long tenantId);
}
