/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import java.util.Optional;

import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;

public interface IProjectBlueprintService {

    Optional<ProjectBlueprintEntity> getBlueprintById(Long blueprintId, Long tenantId);
}
