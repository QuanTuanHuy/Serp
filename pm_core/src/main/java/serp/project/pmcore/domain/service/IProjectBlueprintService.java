/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.entity.project.ProjectBlueprintEntity;

import java.util.Optional;

public interface IProjectBlueprintService {

    Optional<ProjectBlueprintEntity> getBlueprintById(Long blueprintId, Long tenantId);
}
