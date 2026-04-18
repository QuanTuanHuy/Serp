/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuetypescheme.command.manageitems;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeDetailView;
import serp.project.pmcore.application.issuetypescheme.IssueTypeSchemeIssueTypeView;
import serp.project.pmcore.application.shared.cqrs.command.ICommandHandler;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeSchemeService;
import serp.project.pmcore.domain.issuetype.service.IIssueTypeService;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManageIssueTypeSchemeItemsCommandHandler implements ICommandHandler<ManageIssueTypeSchemeItemsCommand, IssueTypeSchemeDetailView> {

    private final IIssueTypeSchemeService issueTypeSchemeService;
    private final IIssueTypeService issueTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IssueTypeSchemeDetailView handle(ManageIssueTypeSchemeItemsCommand command) {
        IssueTypeSchemeEntity updated = issueTypeSchemeService.replaceIssueTypeSchemeItems(
                command.schemeId(),
                command.issueTypeIds(),
                command.tenantId(),
                command.userId()
        );
        return IssueTypeSchemeDetailView.from(updated, buildIssueTypeMap(updated, command.tenantId()));
    }

    private Map<Long, IssueTypeSchemeIssueTypeView> buildIssueTypeMap(IssueTypeSchemeEntity scheme, Long tenantId) {
        Map<Long, IssueTypeSchemeIssueTypeView> issueTypesById = new LinkedHashMap<>();
        if (scheme.getItems() == null) {
            return issueTypesById;
        }
        scheme.getItems().forEach(item -> {
            IssueTypeEntity issueType = issueTypeService.getVisibleIssueTypeById(item.getIssueTypeId(), tenantId);
            issueTypesById.put(item.getIssueTypeId(), IssueTypeSchemeIssueTypeView.from(issueType));
        });
        return issueTypesById;
    }
}
