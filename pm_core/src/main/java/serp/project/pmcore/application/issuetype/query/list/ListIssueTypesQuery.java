/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.query.list;

import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;

public record ListIssueTypesQuery(
        Long tenantId,
        String search,
        Integer hierarchyLevel,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<IssueTypeView>> {
    public IssueTypeListCriteria toCriteria() {
        return IssueTypeListCriteria.builder()
                .search(search)
                .hierarchyLevel(hierarchyLevel)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
