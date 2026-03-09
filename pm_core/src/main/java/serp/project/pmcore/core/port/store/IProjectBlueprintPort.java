/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.port.store;

import serp.project.pmcore.core.domain.entity.ProjectBlueprintEntity;

import java.util.Optional;

public interface IProjectBlueprintPort {
    Optional<ProjectBlueprintEntity> getBlueprintById(Long id, Long tenantId);

    Optional<ProjectBlueprintEntity> getBlueprintByIdIncludingSystem(Long id, Long tenantId);
}
