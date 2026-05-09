/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.project.entity.ProjectCategoryEntity;
import serp.project.pmcore.domain.project.port.IProjectCategoryPort;
import serp.project.pmcore.domain.project.query.ProjectCategoryListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.ProjectCategoryMapper;
import serp.project.pmcore.infrastructure.store.model.ProjectCategoryModel;
import serp.project.pmcore.infrastructure.store.repository.IProjectCategoryRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectCategoryAdapter implements IProjectCategoryPort {

    private final IProjectCategoryRepository projectCategoryRepository;
    private final ProjectCategoryMapper projectCategoryMapper;

    @Override
    public ProjectCategoryEntity createCategory(ProjectCategoryEntity category) {
        return projectCategoryMapper.toEntity(
                projectCategoryRepository.save(projectCategoryMapper.toModel(category))
        );
    }

    @Override
    public Optional<ProjectCategoryEntity> getCategoryById(Long id, Long tenantId) {
        return projectCategoryRepository.findByIdAndTenantId(id, tenantId)
                .map(projectCategoryMapper::toEntity);
    }

    @Override
    public List<ProjectCategoryEntity> getCategoriesByIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return projectCategoryMapper.toEntities(projectCategoryRepository.findAllById(categoryIds));
    }

    @Override
    public Optional<ProjectCategoryEntity> getCategoryByIdIncludingSystem(Long id, Long tenantId) {
        return projectCategoryRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(projectCategoryMapper::toEntity);
    }

    @Override
    public PageResult<ProjectCategoryEntity> listCategories(Long tenantId, ProjectCategoryListCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = switch (criteria.getSortBy()) {
            case "name" -> "name";
            case "updated_at", "updatedAt" -> "updated_at";
            default -> "created_at";
        };
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getPageSize(), Sort.by(direction, sortField));
        Page<ProjectCategoryModel> result = projectCategoryRepository.findAllWithFilters(
                tenantId,
                criteria.getSearch(),
                pageable
        );
        List<ProjectCategoryEntity> entities = projectCategoryMapper.toEntities(result.getContent());
        return new PageResult<>(entities, result.getTotalElements());
    }

    @Override
    public void updateCategory(ProjectCategoryEntity category) {
        projectCategoryRepository.save(projectCategoryMapper.toModel(category));
    }

    @Override
    public boolean existsByNameAndTenantId(String name, Long tenantId) {
        return projectCategoryRepository.existsByTenantIdAndName(tenantId, name);
    }
}
