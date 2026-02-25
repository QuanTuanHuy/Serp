/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectCategoryEntity;
import serp.project.pmcore.core.port.store.IProjectCategoryPort;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectCategoryAdapter implements IProjectCategoryPort {

    @Override
    public Optional<ProjectCategoryEntity> getCategoryById(Long id, Long tenantId) {
        log.warn("ProjectCategoryAdapter.getCategoryById() is a stub — returning empty. " +
                "Implement when category infrastructure is built.");
        return Optional.empty();
    }

    @Override
    public Optional<ProjectCategoryEntity> getCategoryByIdIncludingSystem(Long id, Long tenantId) {
        log.warn("ProjectCategoryAdapter.getCategoryByIdIncludingSystem() is a stub — returning empty. " +
                "Implement when category infrastructure is built.");
        return Optional.empty();
    }

    @Override
    public boolean existsByNameAndTenantId(String name, Long tenantId) {
        log.warn("ProjectCategoryAdapter.existsByNameAndTenantId() is a stub — returning false.");
        return false;
    }
}
