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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.workitem.query.timeline.ListWorkItemTimelineQuery;
import serp.project.pmcore.application.workitem.query.timeline.ListWorkItemTimelineQueryHandler;
import serp.project.pmcore.application.workitem.query.timeline.WorkItemTimelinePageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.dto.WorkItemTimelineCriteria;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.TIMELINE_WORK_ITEMS)
@RequiredArgsConstructor
public class WorkItemTimelineController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final ListWorkItemTimelineQueryHandler listWorkItemTimelineQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<WorkItemTimelinePageView>> listTimelineWorkItems(
            @PathVariable Long projectId,
            @ModelAttribute WorkItemTimelineCriteria criteria,
            @RequestParam(defaultValue = "true") boolean includeDependencies) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        criteria.setProjectId(projectId);
        WorkItemTimelinePageView response = listWorkItemTimelineQueryHandler.handle(new ListWorkItemTimelineQuery(
                tenantId,
                userId,
                authUtils.getCurrentGroups(),
                criteria,
                includeDependencies
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
