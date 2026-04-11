/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetype.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetype.IssueTypeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;

@Service
@RequiredArgsConstructor
public class GetIssueTypeByIdQueryHandler implements IQueryHandler<GetIssueTypeByIdQuery, IssueTypeView> {

    private final IIssueTypeService issueTypeService;

    @Override
    @Transactional(readOnly = true)
    public IssueTypeView handle(GetIssueTypeByIdQuery query) {
        IssueTypeEntity issueType = issueTypeService.getVisibleIssueTypeById(query.issueTypeId(), query.tenantId());
        return IssueTypeView.from(issueType, issueType.isSystem());
    }
}
