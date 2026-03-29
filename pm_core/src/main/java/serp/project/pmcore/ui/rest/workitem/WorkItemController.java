/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workitem;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemCommand;
import serp.project.pmcore.application.workitem.command.create.CreateWorkItemCommandHandler;
import serp.project.pmcore.domain.constant.RestControllerConstants;
import serp.project.pmcore.domain.exception.AccessDeniedException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
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
}
