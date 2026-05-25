/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.project.dto.ProjectUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.port.write.IProjectWritePort;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService implements IProjectService {

    private final IProjectReadPort projectReadPort;
    private final IProjectWritePort projectWritePort;

    @Override
    public ProjectEntity createProject(ProjectEntity project, Long tenantId, Long userId) {
        project.setTenantId(tenantId);
        project.setIsArchived(false);
        project.applyCreate(userId, System.currentTimeMillis());

        return projectWritePort.saveProject(project);
    }

    @Override
    public ProjectEntity saveProject(ProjectEntity entity, Long userId) {
        entity.applyUpdate(userId, System.currentTimeMillis());
        return projectWritePort.saveProject(entity);
    }

    @Override
    public ProjectEntity updateProject(Long projectId, ProjectUpdateData updateData, Long tenantId, Long userId) {
        ProjectEntity existing = getProjectById(projectId, tenantId);

        if (Boolean.TRUE.equals(existing.getIsArchived())) {
            log.warn("[ProjectService] Project is archived: projectId={}, tenantId={}", projectId, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }

        applyUpdate(existing, updateData);
        existing.applyUpdate(userId, System.currentTimeMillis());

        return projectWritePort.saveProject(existing);
    }

    @Override
    public ProjectEntity getProjectById(Long id, Long tenantId) {
        return projectReadPort.getProjectById(id, tenantId)
                .orElseThrow(() -> {
                    log.warn("[ProjectService] Project not found: id={}, tenantId={}", id, tenantId);
                    return ResourceNotFoundException.project(id);
                });
    }

    @Override
    public ProjectEntity getProjectByKey(String key, Long tenantId) {
        return projectReadPort.getProjectByKey(key, tenantId)
                .orElseThrow(() -> {
                    log.warn("[ProjectService] Project not found: key={}, tenantId={}", key, tenantId);
                    return new ResourceNotFoundException(DomainErrorCode.PROJECT_NOT_FOUND);
                });
    }

    @Override
    public void deleteProject(Long id, Long tenantId) {
        // Verify project exists before deleting
        getProjectById(id, tenantId);
        projectWritePort.deleteProjectById(id, tenantId);
    }

    @Override
    public ProjectEntity archiveProject(Long id, Long tenantId, Long userId) {
        ProjectEntity project = getProjectById(id, tenantId);

        if (Boolean.TRUE.equals(project.getIsArchived())) {
            log.warn("[ProjectService] Project is already archived: projectId={}, tenantId={}", id, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ALREADY_ARCHIVED);
        }

        project.setIsArchived(true);
        project.setArchivedAt(System.currentTimeMillis());
        project.applyUpdate(userId, System.currentTimeMillis());

        return projectWritePort.saveProject(project);
    }

    @Override
    public ProjectEntity unarchiveProject(Long id, Long tenantId, Long userId) {
        ProjectEntity project = getProjectById(id, tenantId);

        if (!Boolean.TRUE.equals(project.getIsArchived())) {
            log.warn("[ProjectService] Project is not archived: projectId={}, tenantId={}", id, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_NOT_ARCHIVED);
        }

        project.setIsArchived(false);
        project.setArchivedAt(null);
        project.applyUpdate(userId, System.currentTimeMillis());

        return projectWritePort.saveProject(project);
    }

    private void applyUpdate(ProjectEntity project, ProjectUpdateData updateData) {
        if (updateData.nameProvided()) {
            project.setName(updateData.name());
        }
        if (updateData.keyProvided()) {
            project.setKey(updateData.key());
        }
        if (updateData.descriptionProvided()) {
            project.setDescription(updateData.description());
        }
        if (updateData.leadUserIdProvided()) {
            project.setLeadUserId(updateData.leadUserId());
        }
        if (updateData.categoryIdProvided()) {
            project.setCategoryId(updateData.categoryId());
        }
        if (updateData.urlProvided()) {
            project.setUrl(updateData.url());
        }
        if (updateData.avatarIdProvided()) {
            project.setAvatarId(updateData.avatarId());
        }
    }
}
