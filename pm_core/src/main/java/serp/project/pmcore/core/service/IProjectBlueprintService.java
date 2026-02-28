/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import serp.project.pmcore.core.domain.entity.ProjectBlueprintEntity;

import java.util.Optional;

public interface IProjectBlueprintService {

    Optional<ProjectBlueprintEntity> getBlueprintById(Long blueprintId, Long tenantId);
}
