/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuelinktype.IssueLinkTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.issuelink.service.IIssueLinkTypeService;

@Service
@RequiredArgsConstructor
public class GetIssueLinkTypeByIdQueryHandler implements IQueryHandler<GetIssueLinkTypeByIdQuery, IssueLinkTypeView> {

    private final IIssueLinkTypeService issueLinkTypeService;

    @Override
    @Transactional(readOnly = true)
    public IssueLinkTypeView handle(GetIssueLinkTypeByIdQuery query) {
        return IssueLinkTypeView.from(issueLinkTypeService.getVisibleById(query.id(), query.tenantId()));
    }
}
