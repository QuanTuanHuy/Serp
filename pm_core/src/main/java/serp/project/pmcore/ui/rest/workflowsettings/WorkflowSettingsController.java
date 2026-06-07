/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workflowsettings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.workflow.settings.GetWorkflowSettingsOverviewQuery;
import serp.project.pmcore.application.workflow.settings.GetWorkflowSettingsOverviewQueryHandler;
import serp.project.pmcore.application.workflow.settings.WorkflowSettingsOverviewView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.WORKFLOW_SETTINGS)
@RequiredArgsConstructor
public class WorkflowSettingsController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final GetWorkflowSettingsOverviewQueryHandler getWorkflowSettingsOverviewQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<WorkflowSettingsOverviewView>> getWorkflowSettingsOverview() {
        Long tenantId = requireCurrentTenantId();
        WorkflowSettingsOverviewView response = getWorkflowSettingsOverviewQueryHandler.handle(
                new GetWorkflowSettingsOverviewQuery(tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
