/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.project.query.ProjectListCriteria;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record ListProjectsQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        String search,
        Long categoryId,
        String projectTypeKey,
        Boolean archived,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<ProjectSummaryView>> {

    public ListProjectsQuery {
        groupKeys = groupKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(groupKeys));
    }

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
