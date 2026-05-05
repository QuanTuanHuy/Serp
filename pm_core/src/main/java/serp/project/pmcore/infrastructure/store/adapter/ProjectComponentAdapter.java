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
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.port.IProjectComponentPort;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.ProjectComponentMapper;
import serp.project.pmcore.infrastructure.store.model.ProjectComponentModel;
import serp.project.pmcore.infrastructure.store.repository.IProjectComponentRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectComponentAdapter implements IProjectComponentPort {

    private final IProjectComponentRepository projectComponentRepository;
    private final ProjectComponentMapper projectComponentMapper;

    @Override
    public ProjectComponentEntity createComponent(ProjectComponentEntity component) {
        ProjectComponentModel model = projectComponentMapper.toModel(component);
        if (model == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        return projectComponentMapper.toEntity(
                projectComponentRepository.save(model)
        );
    }

    @Override
    public Optional<ProjectComponentEntity> getComponentById(Long componentId, Long projectId, Long tenantId) {
        return projectComponentRepository.findByIdAndProjectIdAndTenantId(componentId, projectId, tenantId)
                .map(projectComponentMapper::toEntity);
    }

    @Override
    public List<ProjectComponentEntity> getComponentsByIds(List<Long> componentIds, Long projectId, Long tenantId) {
        if (componentIds == null || componentIds.isEmpty()) {
            return List.of();
        }
        return projectComponentMapper.toEntities(
                projectComponentRepository.findAllByIdInAndProjectIdAndTenantId(componentIds, projectId, tenantId)
        );
    }

    @Override
    public PageResult<ProjectComponentEntity> listComponents(Long projectId,
                                                             Long tenantId,
                                                             ProjectComponentListCriteria criteria) {
        Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = switch (criteria.getSortBy()) {
            case "name" -> "name";
            case "updated_at", "updatedAt" -> "updatedAt";
            default -> "createdAt";
        };
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getPageSize(), Sort.by(direction, sortField));
        Page<ProjectComponentModel> result = projectComponentRepository.findAllWithFilters(
                projectId,
                tenantId,
                criteria.getSearch(),
                pageable
        );
        List<ProjectComponentEntity> entities = projectComponentMapper.toEntities(result.getContent());
        return new PageResult<>(entities, result.getTotalElements());
    }

    @Override
    public void updateComponent(ProjectComponentEntity component) {
        ProjectComponentModel model = projectComponentMapper.toModel(component);
        if (model == null) {
            throw new IllegalArgumentException("component must not be null");
        }
        projectComponentRepository.save(model);
    }

    @Override
    public boolean existsByProjectIdAndName(Long projectId, Long tenantId, String name) {
        return projectComponentRepository.existsByProjectIdAndTenantIdAndName(projectId, tenantId, name);
    }

    @Override
    public void deleteComponentLinks(Long componentId, Long tenantId) {
        projectComponentRepository.deleteWorkItemLinks(componentId, tenantId);
    }
}
