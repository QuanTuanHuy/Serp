/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.port;

import java.util.Optional;

import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;

public interface IProjectBlueprintPort {
    Optional<ProjectBlueprintEntity> getBlueprintById(Long id, Long tenantId);

    Optional<ProjectBlueprintEntity> getBlueprintByIdIncludingSystem(Long id, Long tenantId);
}
