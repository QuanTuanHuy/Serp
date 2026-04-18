/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.shared.pagination.PageResult;

@Service
@RequiredArgsConstructor
public class ListIssueTypeSchemesQueryHandler implements IQueryHandler<ListIssueTypeSchemesQuery, PageView<IssueTypeSchemeView>> {

    private final IIssueTypeSchemeService issueTypeSchemeService;

    @Override
    @Transactional(readOnly = true)
    public PageView<IssueTypeSchemeView> handle(ListIssueTypeSchemesQuery query) {
        IssueTypeSchemeListCriteria criteria = query.toCriteria();
        PageResult<IssueTypeSchemeView> result = issueTypeSchemeService.listVisibleIssueTypeSchemes(query.tenantId(), criteria)
                .map(scheme -> IssueTypeSchemeView.from(scheme, scheme.isSystem()));
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
