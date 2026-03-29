/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.query.ProjectListCriteria;

public record ListProjectsQuery(
        Long tenantId,
        String search,
        Long categoryId,
        String projectTypeKey,
        Boolean archived,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<ProjectSummaryView>> {
    public ProjectListCriteria toCriteria() {
        return ProjectListCriteria.builder()
                .search(search)
                .categoryId(categoryId)
                .projectTypeKey(projectTypeKey)
                .archived(archived)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
