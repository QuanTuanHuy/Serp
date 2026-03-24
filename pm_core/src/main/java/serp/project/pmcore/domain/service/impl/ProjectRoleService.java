/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.entity.project.ProjectRoleEntity;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IProjectRolePort;
import serp.project.pmcore.domain.service.IProjectRoleService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectRoleService implements IProjectRoleService {

    private final IProjectRolePort projectRolePort;

    @Override
    public ProjectRoleEntity getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId) {
        return projectRolePort.getProjectRoleByIdIncludingSystem(roleId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.ROLE_NOT_FOUND,
                        "Project role not found: id=" + roleId
                ));
    }

    @Override
    public Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String roleName, Long tenantId) {
        return projectRolePort.getProjectRoleByNameIncludingSystem(roleName, tenantId);
    }
}
