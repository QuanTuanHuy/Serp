/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.resourcecalendar;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.resourcecalendar.settings.GetResourceCalendarSettingsOverviewQuery;
import serp.project.pmcore.application.resourcecalendar.settings.GetResourceCalendarSettingsOverviewQueryHandler;
import serp.project.pmcore.application.resourcecalendar.settings.ResourceCalendarSettingsOverviewView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.RESOURCE_CALENDAR_SETTINGS)
@RequiredArgsConstructor
public class ResourceCalendarSettingsController {
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final GetResourceCalendarSettingsOverviewQueryHandler getResourceCalendarSettingsOverviewQueryHandler;

    @GetMapping("/overview")
    public ResponseEntity<GeneralResponse<ResourceCalendarSettingsOverviewView>> getOverview() {
        Long tenantId = requireCurrentTenantId();
        ResourceCalendarSettingsOverviewView response = getResourceCalendarSettingsOverviewQueryHandler.handle(
                new GetResourceCalendarSettingsOverviewQuery(tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
