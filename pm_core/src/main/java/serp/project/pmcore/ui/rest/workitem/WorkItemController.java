/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemCommand;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.command.delete.DeleteWorkItemCommand;
import serp.project.pmcore.application.workitem.command.delete.DeleteWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.command.delete.DeleteWorkItemResult;
import serp.project.pmcore.application.workitem.command.transition.TransitionWorkItemCommandHandler;
import serp.project.pmcore.application.workitem.command.transition.TransitionWorkItemStatusCommand;
import serp.project.pmcore.application.workitem.command.transition.TransitionWorkItemStatusResult;
import serp.project.pmcore.application.workitem.query.get.GetWorkItemByIdQuery;
import serp.project.pmcore.application.workitem.query.get.GetWorkItemByIdQueryHandler;
import serp.project.pmcore.application.workitem.query.get.WorkItemDetailView;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQuery;
import serp.project.pmcore.application.workitem.query.search.SearchWorkItemsQueryHandler;
import serp.project.pmcore.application.workitem.query.search.WorkItemSearchView;
import serp.project.pmcore.domain.shared.pagination.SortSpec;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.dto.WorkItemSearchCriteria;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.workitem.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.ui.rest.workitem.dto.request.TransitionWorkItemStatusRequest;
import serp.project.pmcore.ui.rest.workitem.dto.response.WorkItemResponse;

@RestController
@RequestMapping(PathConstants.WORKITEMS)
@RequiredArgsConstructor
public class WorkItemController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateWorkItemCommandHandler createWorkItemCommandHandler;
    private final DeleteWorkItemCommandHandler deleteWorkItemCommandHandler;
    private final TransitionWorkItemCommandHandler transitionWorkItemCommandHandler;

    private final SearchWorkItemsQueryHandler searchWorkItemsQueryHandler;
    private final GetWorkItemByIdQueryHandler getWorkItemByIdQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<WorkItemSearchView>>> searchWorkItems(
            @PathVariable Long projectId,
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
    public ResponseEntity<GeneralResponse<WorkItemResponse>> createWorkItem(@PathVariable Long projectId,
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

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<WorkItemDetailView>> getWorkItemById(@PathVariable Long projectId,
                                                                               @PathVariable Long id) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));


        WorkItemDetailView workItemDetail = getWorkItemByIdQueryHandler.handle(
                new GetWorkItemByIdQuery(
                        tenantId,
                        userId,
                        projectId,
                        id
                )
        );
        return ResponseEntity.ok(responseUtils.success(workItemDetail));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteWorkItemResult>> deleteWorkItem(@PathVariable Long projectId,
                                                                                 @PathVariable Long id) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        var result = deleteWorkItemCommandHandler.handle(
                new DeleteWorkItemCommand(
                        projectId,
                        id,
                        tenantId,
                        userId,
                        authUtils.getCurrentGroups()
                )
        );

        return ResponseEntity.ok(responseUtils.success(result));
    }

    @PostMapping("/{id}/transitions")
    public ResponseEntity<GeneralResponse<TransitionWorkItemStatusResult>> transitionWorkItemStatus(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @Valid @RequestBody TransitionWorkItemStatusRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        TransitionWorkItemStatusResult result = transitionWorkItemCommandHandler.handle(
                new TransitionWorkItemStatusCommand(
                        projectId,
                        id,
                        request.getTransitionId(),
                        request.getResolutionId(),
                        request.getFields(),
                        tenantId,
                        userId,
                        authUtils.getCurrentGroups()
                )
        );

        return ResponseEntity.ok(responseUtils.success(result));
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
