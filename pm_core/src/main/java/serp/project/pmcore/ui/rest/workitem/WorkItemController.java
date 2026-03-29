/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemCommand;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQuery;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQueryHandler;
import serp.project.pmcore.application.workitem.query.search.WorkItemSearchView;
import serp.project.pmcore.domain.shared.pagination.SortSpec;
import serp.project.pmcore.domain.shared.constant.RestControllerConstants;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.query.WorkItemSearchCriteria;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.workitem.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.ui.rest.workitem.dto.response.WorkItemResponse;

@RestController
@RequestMapping(RestControllerConstants.WORKITEMS)
@RequiredArgsConstructor
public class WorkItemController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateWorkItemCommandHandler createWorkItemCommandHandler;
    private final SearchWorkItemsQueryHandler searchWorkItemsQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<WorkItemSearchView>>> searchWorkItems(
            @PathVariable("projectId") Long projectId,
            @ModelAttribute WorkItemSearchCriteria criteria,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String nullsPosition) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        WorkItemSearchCriteria searchCriteria = applySearchRequest(projectId, criteria, sortField, sortDirection, nullsPosition);
        PageView<WorkItemSearchView> response = searchWorkItemsQueryHandler.handle(new SearchWorkItemsQuery(
                tenantId,
                userId,
                authUtils.getCurrentGroups(),
                searchCriteria
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping
    public ResponseEntity<GeneralResponse<WorkItemResponse>> createWorkItem(@PathVariable("projectId") Long projectId,
                                                                             @Valid @RequestBody CreateWorkItemRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        WorkItemResponse response = WorkItemResponse.from(createWorkItemCommandHandler.handle(new CreateWorkItemCommand(
                projectId,
                request.getIssueTypeId(),
                request.getSummary(),
                request.getDescription(),
                request.getPriorityId(),
                request.getAssigneeId(),
                request.getParentId(),
                request.getDueDate(),
                request.getTimeOriginalEstimate(),
                request.getSecurityLevelId(),
                request.getCustomFields(),
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        )));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    private WorkItemSearchCriteria applySearchRequest(Long projectId,
                                                      WorkItemSearchCriteria criteria,
                                                      String sortField,
                                                      String sortDirection,
                                                      String nullsPosition) {
        criteria.setProjectId(projectId);
        if (sortField != null || sortDirection != null || nullsPosition != null) {
            criteria.setSort(SortSpec.builder()
                    .field(sortField)
                    .direction(sortDirection == null ? "ASC" : sortDirection)
                    .nullsPosition(nullsPosition == null ? "LAST" : nullsPosition)
                    .build());
        }
        return criteria;
    }
}
