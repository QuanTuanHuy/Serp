/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.port;

import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

import java.util.Optional;

import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;

public interface IProjectBlueprintPort {
    ProjectBlueprintEntity saveBlueprint(ProjectBlueprintEntity blueprint);

    Optional<ProjectBlueprintEntity> getBlueprintById(Long id, Long tenantId);

    Optional<ProjectBlueprintEntity> getBlueprintByIdIncludingSystem(Long id, Long tenantId);

    PageResult<ProjectBlueprintEntity> listBlueprintsIncludingSystem(Long tenantId, ProjectBlueprintListCriteria criteria);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
