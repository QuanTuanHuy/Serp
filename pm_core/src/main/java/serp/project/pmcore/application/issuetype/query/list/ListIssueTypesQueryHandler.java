/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;
import serp.project.pmcore.domain.shared.pagination.PageResult;

@Service
@RequiredArgsConstructor
public class ListIssueTypesQueryHandler implements IQueryHandler<ListIssueTypesQuery, PageView<IssueTypeView>> {

    private final IIssueTypeService issueTypeService;

    @Override
    @Transactional(readOnly = true)
    public PageView<IssueTypeView> handle(ListIssueTypesQuery query) {
        IssueTypeListCriteria criteria = query.toCriteria();
        PageResult<IssueTypeView> result = issueTypeService.listVisibleIssueTypes(query.tenantId(), criteria)
                .map(issueType -> IssueTypeView.from(issueType, issueType.isSystem()));
        int pageSize = criteria.getPageSize();
        int currentPage = criteria.getPage();
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) result.total() / pageSize);

        return new PageView<>(
                result.items(),
                result.total(),
                totalPages,
                currentPage,
                pageSize
        );
    }
}
