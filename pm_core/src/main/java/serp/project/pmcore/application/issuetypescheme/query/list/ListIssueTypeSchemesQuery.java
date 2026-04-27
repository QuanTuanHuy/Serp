/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.query.list;

import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;

public record ListIssueTypeSchemesQuery(
        Long tenantId,
        String search,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<IssueTypeSchemeView>> {
    public IssueTypeSchemeListCriteria toCriteria() {
        return IssueTypeSchemeListCriteria.builder()
                .search(search)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
