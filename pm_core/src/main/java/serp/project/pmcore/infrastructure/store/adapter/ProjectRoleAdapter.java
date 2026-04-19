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

import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.port.IProjectRolePort;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.ProjectRoleMapper;
import serp.project.pmcore.infrastructure.store.model.ProjectRoleModel;
import serp.project.pmcore.infrastructure.store.repository.IProjectRoleRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectRoleAdapter implements IProjectRolePort {

    private final IProjectRoleRepository projectRoleRepository;
    private final ProjectRoleMapper projectRoleMapper;

    @Override
    public ProjectRoleEntity saveProjectRole(ProjectRoleEntity role) {
        return projectRoleMapper.toEntity(projectRoleRepository.save(projectRoleMapper.toModel(role)));
    }

    @Override
    public Optional<ProjectRoleEntity> getProjectRoleById(Long roleId, Long tenantId) {
        return projectRoleRepository.findByIdAndTenantId(roleId, tenantId)
                .map(projectRoleMapper::toEntity);
    }

    @Override
    public Optional<ProjectRoleEntity> getProjectRoleByIdIncludingSystem(Long roleId, Long tenantId) {
        return projectRoleRepository.findByIdAndTenantIdOrSystemTenant(roleId, tenantId)
                .map(projectRoleMapper::toEntity);
    }

    @Override
    public Optional<ProjectRoleEntity> getProjectRoleByNameIncludingSystem(String name, Long tenantId) {
        return projectRoleRepository.findByNameAndTenantIdOrSystemTenant(name, tenantId)
                .stream()
                .findFirst()
                .map(projectRoleMapper::toEntity);
    }

    @Override
    public List<ProjectRoleEntity> getProjectRolesByNameIncludingSystem(String name, Long tenantId) {
        return projectRoleMapper.toEntities(
                projectRoleRepository.findByNameAndTenantIdOrSystemTenant(name, tenantId)
        );
    }

    @Override
    public List<ProjectRoleEntity> getProjectRolesIncludingSystem(Long tenantId) {
        return projectRoleMapper.toEntities(projectRoleRepository.findAllByTenantIdOrSystemTenant(tenantId));
    }

    @Override
    public PageResult<ProjectRoleEntity> getProjectRolesIncludingSystem(Long tenantId, ProjectRoleListCriteria criteria) {
        Sort.Direction direction = "desc".equalsIgnoreCase(criteria.getSortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = switch (criteria.getSortBy()) {
            case "created_at", "createdAt" -> "createdAt";
            case "updated_at", "updatedAt" -> "updatedAt";
            default -> "name";
        };
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getPageSize(), Sort.by(direction, sortField));
        Page<ProjectRoleModel> result = projectRoleRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(projectRoleMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public boolean existsByNameAndTenantId(String name, Long tenantId) {
        return projectRoleRepository.existsByTenantIdAndName(tenantId, name);
    }
}
