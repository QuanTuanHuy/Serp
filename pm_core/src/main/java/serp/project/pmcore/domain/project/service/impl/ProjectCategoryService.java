/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.project.dto.ProjectCategoryUpdateData;
import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.port.read.IProjectReadPort;
import serp.project.pmcore.domain.project.query.ProjectCategoryListCriteria;
import serp.project.pmcore.domain.project.service.IProjectCategoryService;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.domain.shared.util.TextNormalizationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectCategoryService implements IProjectCategoryService {

    private static final int CATEGORY_NAME_MAX_LENGTH = 255;
    private static final int CATEGORY_DESCRIPTION_MAX_LENGTH = 2000;

    private final IProjectCategoryPort projectCategoryPort;
    private final IProjectReadPort projectReadPort;

    @Override
    public ProjectCategoryEntity createCategory(ProjectCategoryEntity category, Long tenantId, Long userId) {
        String name = TextNormalizationUtils.normalizeRequiredText(
                category.getName(),
                "name",
                CATEGORY_NAME_MAX_LENGTH
        );
        if (projectCategoryPort.existsByNameAndTenantId(name, tenantId)) {
            log.warn("Project category name already exists: name={}, tenantId={}", name, tenantId);
            throw new BusinessRuleViolationException(DomainErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        category.setTenantId(tenantId);
        category.setName(name);
        category.setDescription(TextNormalizationUtils.normalizeOptionalText(
                category.getDescription(),
                CATEGORY_DESCRIPTION_MAX_LENGTH,
                "description"
        ));
        category.setIsSystem(false);
        category.setDeletedAt(null);
        category.applyCreate(userId, System.currentTimeMillis());

        return projectCategoryPort.createCategory(category);
    }

    @Override
    public ProjectCategoryEntity getCategoryById(Long categoryId, Long tenantId) {
        return projectCategoryPort.getCategoryById(categoryId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Project category not found: id={}, tenantId={}", categoryId, tenantId);
                    return ResourceNotFoundException.category(categoryId);
                });
    }

    @Override
    public List<ProjectCategoryEntity> getCategoriesByIds(List<Long> categoryIds) {
        return projectCategoryPort.getCategoriesByIds(categoryIds);
    }

    @Override
    public PageResult<ProjectCategoryEntity> listCategories(Long tenantId, ProjectCategoryListCriteria criteria) {
        return projectCategoryPort.listCategories(tenantId, criteria);
    }

    @Override
    public ProjectCategoryEntity updateCategory(Long categoryId,
                                                ProjectCategoryUpdateData data,
                                                Long tenantId,
                                                Long userId) {
        ProjectCategoryEntity existing = getCategoryById(categoryId, tenantId);

        if (data.nameProvided()) {
            String newName = TextNormalizationUtils.normalizeRequiredText(
                    data.name(),
                    "name",
                    CATEGORY_NAME_MAX_LENGTH
            );
            if (!newName.equalsIgnoreCase(existing.getName())
                    && projectCategoryPort.existsByNameAndTenantId(newName, tenantId)) {
                throw new BusinessRuleViolationException(DomainErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
            }
            existing.setName(newName);
        }

        if (data.descriptionProvided()) {
            existing.setDescription(TextNormalizationUtils.normalizeOptionalText(
                    data.description(),
                    CATEGORY_DESCRIPTION_MAX_LENGTH,
                    "description"
            ));
        }

        existing.applyUpdate(userId, System.currentTimeMillis());
        projectCategoryPort.updateCategory(existing);
        return existing;
    }

    @Override
    public ProjectCategoryEntity deleteCategory(Long categoryId, Long tenantId, Long userId) {
        ProjectCategoryEntity existing = getCategoryById(categoryId, tenantId);
        if (projectReadPort.existsActiveProjectByCategoryId(categoryId, tenantId)) {
            throw new BusinessRuleViolationException(DomainErrorCode.CATEGORY_IN_USE);
        }

        long now = System.currentTimeMillis();
        existing.setDeletedAt(now);
        existing.applyUpdate(userId, now);
        projectCategoryPort.updateCategory(existing);
        return existing;
    }

}
