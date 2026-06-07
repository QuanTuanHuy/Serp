/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.component.query.list;

import serp.project.pmcore.application.project.component.ProjectComponentView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.query.ProjectComponentListCriteria;

import java.util.Set;

public record ListProjectComponentsQuery(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        String search,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<ProjectComponentView>> {
    public ListProjectComponentsQuery {
        groupKeys = groupKeys == null ? Set.of() : Set.copyOf(groupKeys);
    }

    public ProjectComponentListCriteria toCriteria() {
        return ProjectComponentListCriteria.builder()
                .search(search)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
