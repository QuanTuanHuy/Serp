/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;

public interface IProjectRoleService {
    ProjectRoleEntity getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId);

    Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String roleName, Long tenantId);

    List<ProjectRoleEntity> getProjectRolesByNameIncludingSystem(String roleName, Long tenantId);
}
