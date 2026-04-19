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

import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.port.IProjectBlueprintPort;
import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.ProjectBlueprintMapper;
import serp.project.pmcore.infrastructure.store.model.ProjectBlueprintModel;
import serp.project.pmcore.infrastructure.store.repository.IProjectBlueprintRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectBlueprintAdapter implements IProjectBlueprintPort {

    private final IProjectBlueprintRepository projectBlueprintRepository;
    private final ProjectBlueprintMapper projectBlueprintMapper;

    @Override
    public ProjectBlueprintEntity saveBlueprint(ProjectBlueprintEntity blueprint) {
        return projectBlueprintMapper.toEntity(
                projectBlueprintRepository.save(projectBlueprintMapper.toModel(blueprint))
        );
    }

    @Override
    public Optional<ProjectBlueprintEntity> getBlueprintById(Long id, Long tenantId) {
        return projectBlueprintRepository.findByIdAndTenantId(id, tenantId)
                .map(projectBlueprintMapper::toEntity);
    }

    @Override
    public Optional<ProjectBlueprintEntity> getBlueprintByIdIncludingSystem(Long id, Long tenantId) {
        return projectBlueprintRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(projectBlueprintMapper::toEntity);
    }

    @Override
    public PageResult<ProjectBlueprintEntity> listBlueprintsIncludingSystem(Long tenantId, ProjectBlueprintListCriteria criteria) {
        Sort.Direction direction = "desc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = switch (criteria.getSortBy()) {
            case "created_at", "createdAt" -> "createdAt";
            case "updated_at", "updatedAt" -> "updatedAt";
            default -> "name";
        };
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getPageSize(), Sort.by(direction, sortField));
        Page<ProjectBlueprintModel> result = projectBlueprintRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getProjectTypeKey(),
                criteria.getIsSystem(),
                pageable
        );
        List<ProjectBlueprintEntity> entities = projectBlueprintMapper.toEntities(result.getContent());
        return new PageResult<>(entities, result.getTotalElements());
    }

    @Override
    public boolean existsByNameAndTenantId(String name, Long tenantId) {
        return projectBlueprintRepository.existsByTenantIdAndName(tenantId, name);
    }
}
