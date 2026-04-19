/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.query.list;

import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.domain.project.query.ProjectRoleListCriteria;

public record ListProjectRoleQuery(
        Long tenantId,
        String search,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<ProjectRoleView>> {
    public ProjectRoleListCriteria toCriteria() {
        return ProjectRoleListCriteria.builder()
                .search(search)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
