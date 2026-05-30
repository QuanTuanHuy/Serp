/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.workitem.query.calendar.ListWorkItemScheduleCalendarQuery;
import serp.project.pmcore.application.workitem.query.calendar.ListWorkItemScheduleCalendarQueryHandler;
import serp.project.pmcore.application.workitem.query.calendar.WorkItemScheduleCalendarPageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.dto.WorkItemScheduleCalendarCriteria;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PROJECT_CALENDAR)
@RequiredArgsConstructor
public class WorkItemCalendarController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final ListWorkItemScheduleCalendarQueryHandler listWorkItemScheduleCalendarQueryHandler;

    @GetMapping("/schedule-allocations")
    public ResponseEntity<GeneralResponse<WorkItemScheduleCalendarPageView>> listScheduleCalendarItems(
            @PathVariable Long projectId,
            @ModelAttribute WorkItemScheduleCalendarCriteria criteria) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        criteria.setProjectId(projectId);
        WorkItemScheduleCalendarPageView response = listWorkItemScheduleCalendarQueryHandler.handle(
                new ListWorkItemScheduleCalendarQuery(
                        tenantId,
                        userId,
                        authUtils.getCurrentGroups(),
                        criteria
                )
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
