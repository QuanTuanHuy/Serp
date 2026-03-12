/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import serp.project.pmcore.core.domain.constant.RestControllerConstants;
import serp.project.pmcore.core.domain.dto.filter.SortField;
import serp.project.pmcore.core.domain.dto.filter.WorkItemFilterRequest;
import serp.project.pmcore.core.domain.dto.request.CreateWorkItemRequest;
import serp.project.pmcore.core.domain.dto.response.GeneralResponse;
import serp.project.pmcore.core.domain.dto.response.WorkItemResponse;
import serp.project.pmcore.core.exception.AppException;
import serp.project.pmcore.core.exception.ErrorCode;
import serp.project.pmcore.core.usecase.WorkItemUseCase;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.kernel.utils.ResponseUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(RestControllerConstants.WORKITEMS)
@RequiredArgsConstructor
@Slf4j
public class WorkItemController {

    private final WorkItemUseCase workItemUseCase;
    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> createWorkItem(
            @Valid @RequestBody CreateWorkItemRequest request,
            @PathVariable Long projectId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        request.setProjectId(projectId);
        WorkItemResponse response = workItemUseCase.createWorkItem(request, tenantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<?>> getWorkItemById(@PathVariable Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        WorkItemResponse response = workItemUseCase.getWorkItemById(id, tenantId);
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<?>> getWorkItems(
            @PathVariable Long projectId,
            @RequestParam(required = false) List<Long> statusIds,
            @RequestParam(required = false) List<Long> priorityIds,
            @RequestParam(required = false) List<Long> issueTypeIds,
            @RequestParam(required = false) List<Long> assigneeIds,
            @RequestParam(required = false) List<Long> reporterIds,
            @RequestParam(required = false) List<Long> resolutionIds,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean unassigned,
            @RequestParam(required = false) Boolean unresolved,
            @RequestParam(required = false) Long dueDateFrom,
            @RequestParam(required = false) Long dueDateTo,
            @RequestParam(required = false) Long createdFrom,
            @RequestParam(required = false) Long createdTo,
            @RequestParam(required = false) Long updatedFrom,
            @RequestParam(required = false) Long updatedTo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isOverdue,
            @RequestParam(required = false) Boolean hasTimeLogged,
            @RequestParam(required = false) Boolean enriched,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "rank") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        SortField sort = SortField.builder()
                .field(sortBy)
                .direction(sortDirection)
                .build();

        WorkItemFilterRequest filter = WorkItemFilterRequest.builder()
                .projectId(projectId)
                .statusIds(statusIds)
                .priorityIds(priorityIds)
                .issueTypeIds(issueTypeIds)
                .assigneeIds(assigneeIds)
                .reporterIds(reporterIds)
                .resolutionIds(resolutionIds)
                .parentId(parentId)
                .unassigned(unassigned)
                .unresolved(unresolved)
                .dueDateFrom(dueDateFrom)
                .dueDateTo(dueDateTo)
                .createdFrom(createdFrom)
                .createdTo(createdTo)
                .updatedFrom(updatedFrom)
                .updatedTo(updatedTo)
                .keyword(keyword)
                .isOverdue(isOverdue)
                .hasTimeLogged(hasTimeLogged)
                .enriched(enriched)
                .page(page)
                .pageSize(pageSize)
                .sort(sort)
                .build();

        Map<String, Object> response = workItemUseCase.getWorkItems(tenantId, filter);
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
