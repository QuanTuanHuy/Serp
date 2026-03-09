/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import serp.project.pmcore.core.domain.dto.request.GetProjectParams;
import serp.project.pmcore.core.domain.entity.ProjectEntity;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.port.store.IProjectCategoryPort;
import serp.project.pmcore.core.port.store.IProjectPort;
import serp.project.pmcore.core.service.IProjectService;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService implements IProjectService {

    private final IProjectPort projectPort;
    private final IProjectCategoryPort projectCategoryPort;

    private static final Pattern PROJECT_KEY_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]{1,9}$");

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
            throw new AppException(ErrorCode.PROJECT_ARCHIVED);
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
            validateCategoryExists(updateData.getCategoryId(), tenantId);
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
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
    }

    @Override
    public ProjectEntity getProjectByKey(String key, Long tenantId) {
        return projectPort.getProjectByKey(key, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));
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
            throw new AppException(ErrorCode.PROJECT_ALREADY_ARCHIVED);
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
            throw new AppException(ErrorCode.PROJECT_NOT_ARCHIVED);
        }

        project.setIsArchived(false);
        project.setArchivedAt(null);
        project.setUpdatedBy(userId);
        project.setUpdatedAt(System.currentTimeMillis());

        return projectPort.saveProject(project);
    }

    @Override
    public void validateKeyFormat(String key) {
        if (key == null || !PROJECT_KEY_PATTERN.matcher(key).matches()) {
            throw new AppException(ErrorCode.PROJECT_KEY_INVALID_FORMAT);
        }
    }

    @Override
    public void validateKeyUniqueness(String key, Long tenantId) {
        if (projectPort.existsByKeyAndTenantId(key, tenantId)) {
            throw new AppException(ErrorCode.PROJECT_KEY_ALREADY_EXISTS);
        }
    }

    @Override
    public void validateCategoryExists(Long categoryId, Long tenantId) {
        if (categoryId != null) {
            projectCategoryPort.getCategoryByIdIncludingSystem(categoryId, tenantId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        }
    }
}
