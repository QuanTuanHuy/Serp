/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.blueprint.query.list;

import serp.project.pmcore.application.blueprint.ProjectBlueprintView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.blueprint.query.ProjectBlueprintListCriteria;

public record ListProjectBlueprintsQuery(
        Long tenantId,
        String search,
        String projectTypeKey,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<ProjectBlueprintView>> {
    public ProjectBlueprintListCriteria toCriteria() {
        return ProjectBlueprintListCriteria.builder()
                .search(search)
                .projectTypeKey(projectTypeKey)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
