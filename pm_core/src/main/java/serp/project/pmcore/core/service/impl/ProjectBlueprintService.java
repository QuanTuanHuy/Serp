/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.core.domain.entity.ProjectBlueprintEntity;
import serp.project.pmcore.core.port.store.IProjectBlueprintPort;
import serp.project.pmcore.core.service.IProjectBlueprintService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectBlueprintService implements IProjectBlueprintService {

    private final IProjectBlueprintPort projectBlueprintPort;

    @Override
    public Optional<ProjectBlueprintEntity> getBlueprintById(Long blueprintId, Long tenantId) {
        return projectBlueprintPort.getBlueprintByIdIncludingSystem(blueprintId, tenantId);
    }
}
