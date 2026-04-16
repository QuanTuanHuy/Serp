/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.project.dto.ProjectRoleUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.port.IProjectRolePort;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectRoleService implements IProjectRoleService {

    private final IProjectRolePort projectRolePort;
    private final IPermissionSchemeEntryPort permissionSchemeEntryPort;

    private static final int ROLE_NAME_MAX_LENGTH = 255;
    private static final int ROLE_DESCRIPTION_MAX_LENGTH = 2000;

    @Override
    public ProjectRoleEntity createProjectRole(ProjectRoleEntity role, Long tenantId, Long userId) {
        String name = normalizeRequiredText(role.getName(), "name", ROLE_NAME_MAX_LENGTH);
        if (projectRolePort.existsByNameAndTenantId(name, tenantId)) {
            log.warn("Project role name already exists: name={}, tenantId={}", name, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.ROLE_NAME_ALREADY_EXISTS);
        }

        role.setTenantId(tenantId);
        role.setName(name);
        role.setDescription(normalizeOptionalText(role.getDescription(), ROLE_DESCRIPTION_MAX_LENGTH, "description"));
        role.setIsSystem(false);
        role.setDeletedAt(null);
        role.applyCreate(userId, System.currentTimeMillis());
        return projectRolePort.saveProjectRole(role);
    }

    @Override
    public ProjectRoleEntity getProjectRoleById(Long roleId, Long tenantId) {
        return projectRolePort.getProjectRoleById(roleId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.role(roleId));
    }

    @Override
    public ProjectRoleEntity getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId) {
        return projectRolePort.getProjectRoleByIdIncludingSystem(roleId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.role(roleId));
    }

    @Override
    public Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String roleName, Long tenantId) {
        return projectRolePort.getProjectRoleByNameIncludingSystem(roleName, tenantId);
    }

    @Override
    public List<ProjectRoleEntity> getProjectRolesByNameIncludingSystem(String roleName, Long tenantId) {
        return projectRolePort.getProjectRolesByNameIncludingSystem(roleName, tenantId);
    }

    @Override
    public PageResult<ProjectRoleEntity> listVisibleProjectRoles(Long tenantId, ProjectRoleListCriteria criteria) {
        return projectRolePort.getProjectRolesIncludingSystem(tenantId, criteria);
    }

    @Override
    public ProjectRoleEntity updateProjectRole(Long roleId, ProjectRoleUpdateData data, Long tenantId, Long userId) {
        ProjectRoleEntity existing = getProjectRoleById(roleId, tenantId);
        ensureWritable(existing);

        if (data.nameProvided()) {
            String newName = normalizeRequiredText(data.name(), "name", ROLE_NAME_MAX_LENGTH);
            if (!newName.equalsIgnoreCase(existing.getName())
                    && projectRolePort.existsByNameAndTenantId(newName, tenantId)) {
                throw new BusinessRuleViolationException(DomainErrorCode.ROLE_NAME_ALREADY_EXISTS);
            }
            existing.setName(newName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(normalizeOptionalText(data.description(), ROLE_DESCRIPTION_MAX_LENGTH, "description"));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        return projectRolePort.saveProjectRole(existing);
    }

    @Override
    public ProjectRoleEntity deleteProjectRole(Long roleId, Long tenantId, Long userId) {
        ProjectRoleEntity existing = getProjectRoleById(roleId, tenantId);
        ensureWritable(existing);
        if (permissionSchemeEntryPort.existsByProjectRoleId(tenantId, roleId)) {
            throw new BusinessRuleViolationException(DomainErrorCode.ROLE_IN_USE_BY_PERMISSION);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        return projectRolePort.saveProjectRole(existing);
    }

    private void ensureWritable(ProjectRoleEntity role) {
        if (Boolean.TRUE.equals(role.getIsSystem())) {
            throw new BusinessRuleViolationException(DomainErrorCode.ROLE_IS_SYSTEM);
        }
    }

    private String normalizeRequiredText(String value, String fieldName, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    private String normalizeOptionalText(String value, int maxLength, String fieldName) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }
}
