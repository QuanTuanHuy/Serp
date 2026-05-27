/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.prioritysettings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.priority.settings.GetPrioritySettingsOverviewQuery;
import serp.project.pmcore.application.priority.settings.GetPrioritySettingsOverviewQueryHandler;
import serp.project.pmcore.application.priority.settings.PrioritySettingsOverviewView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PRIORITY_SETTINGS)
@RequiredArgsConstructor
public class PrioritySettingsController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final GetPrioritySettingsOverviewQueryHandler getPrioritySettingsOverviewQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<PrioritySettingsOverviewView>> getPrioritySettingsOverview() {
        Long tenantId = requireCurrentTenantId();
        PrioritySettingsOverviewView response = getPrioritySettingsOverviewQueryHandler.handle(
                new GetPrioritySettingsOverviewQuery(tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
