/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.ProjectBlueprintEntity;
import serp.project.pmcore.core.port.store.IProjectBlueprintPort;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectBlueprintAdapter implements IProjectBlueprintPort {

    @Override
    public Optional<ProjectBlueprintEntity> getBlueprintById(Long id, Long tenantId) {
        log.warn("ProjectBlueprintAdapter.getBlueprintById() is a stub — returning empty. " +
                "Implement when blueprint infrastructure is built.");
        return Optional.empty();
    }

    @Override
    public Optional<ProjectBlueprintEntity> getBlueprintByIdIncludingSystem(Long id, Long tenantId) {
        log.warn("ProjectBlueprintAdapter.getBlueprintByIdIncludingSystem() is a stub — returning empty. " +
                "Implement when blueprint infrastructure is built.");
        return Optional.empty();
    }
}
