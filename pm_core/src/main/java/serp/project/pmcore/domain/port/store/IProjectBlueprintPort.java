/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.project.ProjectBlueprintEntity;

import java.util.Optional;

public interface IProjectBlueprintPort {
    Optional<ProjectBlueprintEntity> getBlueprintById(Long id, Long tenantId);

    Optional<ProjectBlueprintEntity> getBlueprintByIdIncludingSystem(Long id, Long tenantId);
}
