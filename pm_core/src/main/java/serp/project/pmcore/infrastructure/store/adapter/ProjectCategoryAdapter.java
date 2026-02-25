/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectCategoryEntity;
import serp.project.pmcore.core.port.store.IProjectCategoryPort;
import serp.project.pmcore.infrastructure.store.mapper.ProjectCategoryMapper;
import serp.project.pmcore.infrastructure.store.repository.IProjectCategoryRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectCategoryAdapter implements IProjectCategoryPort {

    private final IProjectCategoryRepository projectCategoryRepository;
    private final ProjectCategoryMapper projectCategoryMapper;

    @Override
    public Optional<ProjectCategoryEntity> getCategoryById(Long id, Long tenantId) {
        return projectCategoryRepository.findByIdAndTenantId(id, tenantId)
                .map(projectCategoryMapper::toEntity);
    }

    @Override
    public Optional<ProjectCategoryEntity> getCategoryByIdIncludingSystem(Long id, Long tenantId) {
        return projectCategoryRepository.findByIdAndTenantIdOrSystemTenant(id, tenantId)
                .map(projectCategoryMapper::toEntity);
    }

    @Override
    public boolean existsByNameAndTenantId(String name, Long tenantId) {
        return projectCategoryRepository.existsByTenantIdAndName(tenantId, name);
    }
}
