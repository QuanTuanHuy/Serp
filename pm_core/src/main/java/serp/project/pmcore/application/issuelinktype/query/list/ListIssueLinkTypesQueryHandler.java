/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.query.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.shared.pagination.PageViews;
import serp.project.pmcore.domain.issuelink.query.IssueLinkTypeListCriteria;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;

@Service
@RequiredArgsConstructor
public class ListIssueLinkTypesQueryHandler implements IQueryHandler<ListIssueLinkTypesQuery, PageView<IssueLinkTypeView>> {

    private final IIssueLinkTypeService issueLinkTypeService;

    @Override
    @Transactional(readOnly = true)
    public PageView<IssueLinkTypeView> handle(ListIssueLinkTypesQuery query) {
        IssueLinkTypeListCriteria criteria = query.toCriteria();
        return PageViews.from(
                issueLinkTypeService.listVisible(query.tenantId(), criteria),
                criteria,
                IssueLinkTypeView::from
        );
    }
}
