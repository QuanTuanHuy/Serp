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
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;

@Service
@RequiredArgsConstructor
public class ListIssueTypesQueryHandler implements IQueryHandler<ListIssueTypesQuery, PageView<IssueTypeView>> {

    private final IIssueTypeService issueTypeService;

    @Override
    @Transactional(readOnly = true)
    public PageView<IssueTypeView> handle(ListIssueTypesQuery query) {
        IssueTypeListCriteria criteria = query.toCriteria();
        return PageViews.from(
                issueTypeService.listVisibleIssueTypes(query.tenantId(), criteria),
                criteria,
                issueType -> IssueTypeView.from(issueType, issueType.isSystem())
        );
    }
}
