/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.issuetypesettings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.issuetype.settings.GetIssueTypeSettingsOverviewQuery;
import serp.project.pmcore.application.issuetype.settings.GetIssueTypeSettingsOverviewQueryHandler;
import serp.project.pmcore.application.issuetype.settings.IssueTypeSettingsOverviewView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.ISSUE_TYPE_SETTINGS)
@RequiredArgsConstructor
public class IssueTypeSettingsController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final GetIssueTypeSettingsOverviewQueryHandler getIssueTypeSettingsOverviewQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<IssueTypeSettingsOverviewView>> getIssueTypeSettingsOverview() {
        Long tenantId = requireCurrentTenantId();
        IssueTypeSettingsOverviewView response = getIssueTypeSettingsOverviewQueryHandler.handle(
                new GetIssueTypeSettingsOverviewQuery(tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
