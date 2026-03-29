/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import serp.project.pmcore.application.project.query.list.model.GetProjectParams;
import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IProjectPort;
import serp.project.pmcore.domain.service.IProjectService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService implements IProjectService {

    private final IProjectPort projectPort;

    @Override
    public ProjectEntity createProject(ProjectEntity project, Long tenantId, Long userId) {
        project.setTenantId(tenantId);
        project.setIsArchived(false);
        project.setCreatedBy(userId);
        project.setUpdatedBy(userId);
        project.setCreatedAt(System.currentTimeMillis());
        project.setUpdatedAt(System.currentTimeMillis());

        return projectPort.saveProject(project);
    }

    @Override
    public ProjectEntity saveProject(ProjectEntity entity, Long userId) {
        entity.setUpdatedBy(userId);
        entity.setUpdatedAt(System.currentTimeMillis());
        return projectPort.saveProject(entity);
    }

    @Override
    public ProjectEntity updateProject(Long projectId, ProjectEntity updateData, Long tenantId, Long userId) {
        ProjectEntity existing = getProjectById(projectId, tenantId);

        if (Boolean.TRUE.equals(existing.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ARCHIVED);
        }

        if (updateData.getName() != null) {
            existing.setName(updateData.getName());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getLeadUserId() != null) {
            existing.setLeadUserId(updateData.getLeadUserId());
        }
        if (updateData.getCategoryId() != null) {
            existing.setCategoryId(updateData.getCategoryId());
        }
        if (updateData.getUrl() != null) {
            existing.setUrl(updateData.getUrl());
        }
        if (updateData.getAvatarId() != null) {
            existing.setAvatarId(updateData.getAvatarId());
        }

        existing.setUpdatedBy(userId);
        existing.setUpdatedAt(System.currentTimeMillis());

        return projectPort.saveProject(existing);
    }

    @Override
    public ProjectEntity getProjectById(Long id, Long tenantId) {
        return projectPort.getProjectById(id, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.project(id));
    }

    @Override
    public ProjectEntity getProjectByKey(String key, Long tenantId) {
        return projectPort.getProjectByKey(key, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(DomainErrorCode.PROJECT_NOT_FOUND));
    }

    @Override
    public Pair<List<ProjectEntity>, Long> getProjects(Long tenantId, GetProjectParams params) {
        return projectPort.getProjects(
                tenantId,
                params.getSearch(),
                params.getCategoryId(),
                params.getProjectTypeKey(),
                params.getArchived(),
                params.getPage(),
                params.getPageSize(),
                params.getSortBy(),
                params.getSortDirection()
        );
    }

    @Override
    public void deleteProject(Long id, Long tenantId) {
        // Verify project exists before deleting
        getProjectById(id, tenantId);
        projectPort.deleteProjectById(id, tenantId);
    }

    @Override
    public ProjectEntity archiveProject(Long id, Long tenantId, Long userId) {
        ProjectEntity project = getProjectById(id, tenantId);

        if (Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_ALREADY_ARCHIVED);
        }

        project.setIsArchived(true);
        project.setArchivedAt(System.currentTimeMillis());
        project.setUpdatedBy(userId);
        project.setUpdatedAt(System.currentTimeMillis());

        return projectPort.saveProject(project);
    }

    @Override
    public ProjectEntity unarchiveProject(Long id, Long tenantId, Long userId) {
        ProjectEntity project = getProjectById(id, tenantId);

        if (!Boolean.TRUE.equals(project.getIsArchived())) {
            throw new BusinessRuleViolationException(DomainErrorCode.PROJECT_NOT_ARCHIVED);
        }

        project.setIsArchived(false);
        project.setArchivedAt(null);
        project.setUpdatedBy(userId);
        project.setUpdatedAt(System.currentTimeMillis());

        return projectPort.saveProject(project);
    }

}
