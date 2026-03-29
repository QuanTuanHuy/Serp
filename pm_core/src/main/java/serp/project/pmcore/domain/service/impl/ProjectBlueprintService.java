/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.port.IProjectBlueprintPort;
import serp.project.pmcore.domain.service.IProjectBlueprintService;

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
