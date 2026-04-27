/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.blueprint.service;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.blueprint.dto.ProjectBlueprintUpdateData;
import serp.project.pmcore.domain.blueprint.entity.BlueprintSchemeDefaultEntity;
import serp.project.pmcore.domain.blueprint.entity.ProjectBlueprintEntity;
import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;

public interface IProjectBlueprintService {

    ProjectBlueprintEntity createBlueprint(ProjectBlueprintEntity blueprint, Long tenantId, Long userId);

    ProjectBlueprintEntity getBlueprintByIdIncludingSystemOrThrow(Long blueprintId, Long tenantId);

    List<BlueprintSchemeDefaultEntity> getBlueprintDefaultsIncludingSystem(Long blueprintId, Long tenantId);

    PageResult<ProjectBlueprintEntity> listBlueprintsIncludingSystem(Long tenantId, ProjectBlueprintListCriteria criteria);

    ProjectBlueprintEntity updateBlueprint(Long blueprintId, ProjectBlueprintUpdateData data, Long tenantId, Long userId);

    ProjectBlueprintEntity deleteBlueprint(Long blueprintId, Long tenantId, Long userId);

    Optional<ProjectBlueprintEntity> getBlueprintById(Long blueprintId, Long tenantId);
}
