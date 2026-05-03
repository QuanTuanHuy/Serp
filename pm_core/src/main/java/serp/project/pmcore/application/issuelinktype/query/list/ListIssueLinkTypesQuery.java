/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.query.list;

import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQuery;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuelink.query.IssueLinkTypeListCriteria;

public record ListIssueLinkTypesQuery(
        Long tenantId,
        String search,
        Boolean isSystem,
        Integer page,
        Integer pageSize,
        String sortBy,
        String sortDirection
) implements IQuery<PageView<IssueLinkTypeView>> {
    public IssueLinkTypeListCriteria toCriteria() {
        return IssueLinkTypeListCriteria.builder()
                .search(search)
                .isSystem(isSystem)
                .page(page)
                .pageSize(pageSize)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }
}
