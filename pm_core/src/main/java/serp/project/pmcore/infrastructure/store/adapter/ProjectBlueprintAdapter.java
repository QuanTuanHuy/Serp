/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectBlueprintEntity;
import serp.project.pmcore.core.port.store.IProjectBlueprintPort;
import serp.project.pmcore.infrastructure.store.mapper.ProjectBlueprintMapper;
import serp.project.pmcore.infrastructure.store.repository.IProjectBlueprintRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectBlueprintAdapter implements IProjectBlueprintPort {

    private final IProjectBlueprintRepository projectBlueprintRepository;
    private final ProjectBlueprintMapper projectBlueprintMapper;

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
}
