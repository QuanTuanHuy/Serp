/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.worklog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.worklog.WorklogDetailView;
import serp.project.pmcore.application.worklog.WorklogListPageView;
import serp.project.pmcore.application.worklog.command.create.CreateWorklogCommand;
import serp.project.pmcore.application.worklog.command.create.CreateWorklogCommandHandler;
import serp.project.pmcore.application.worklog.command.create.CreateWorklogResult;
import serp.project.pmcore.application.worklog.command.delete.DeleteWorklogCommand;
import serp.project.pmcore.application.worklog.command.delete.DeleteWorklogCommandHandler;
import serp.project.pmcore.application.worklog.command.delete.DeleteWorklogResult;
import serp.project.pmcore.application.worklog.command.update.UpdateWorklogCommand;
import serp.project.pmcore.application.worklog.command.update.UpdateWorklogCommandHandler;
import serp.project.pmcore.application.worklog.command.update.UpdateWorklogResult;
import serp.project.pmcore.application.worklog.query.get.GetWorklogByIdQuery;
import serp.project.pmcore.application.worklog.query.get.GetWorklogByIdQueryHandler;
import serp.project.pmcore.application.worklog.query.list.ListWorklogsQuery;
import serp.project.pmcore.application.worklog.query.list.ListWorklogsQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.worklog.dto.request.CreateWorklogRequest;
import serp.project.pmcore.ui.rest.worklog.dto.request.UpdateWorklogRequest;

@RestController
@RequestMapping(PathConstants.WORKLOGS)
@RequiredArgsConstructor
public class WorklogController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateWorklogCommandHandler createWorklogCommandHandler;
    private final UpdateWorklogCommandHandler updateWorklogCommandHandler;
    private final DeleteWorklogCommandHandler deleteWorklogCommandHandler;
    private final GetWorklogByIdQueryHandler getWorklogByIdQueryHandler;
    private final ListWorklogsQueryHandler listWorklogsQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<CreateWorklogResult>> createWorklog(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @Valid @RequestBody CreateWorklogRequest request) {
        CreateWorklogResult response = createWorklogCommandHandler.handle(new CreateWorklogCommand(
                projectId,
                workItemId,
                request.getTimeSpent(),
                request.getStartDate(),
                request.getComment(),
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{worklogId}")
    public ResponseEntity<GeneralResponse<UpdateWorklogResult>> updateWorklog(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @PathVariable Long worklogId,
            @Valid @RequestBody UpdateWorklogRequest request) {
        UpdateWorklogResult response = updateWorklogCommandHandler.handle(new UpdateWorklogCommand(
                projectId,
                workItemId,
                worklogId,
                request.getTimeSpent(),
                request.getStartDate(),
                request.getComment(),
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{worklogId}")
    public ResponseEntity<GeneralResponse<DeleteWorklogResult>> deleteWorklog(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @PathVariable Long worklogId) {
        DeleteWorklogResult response = deleteWorklogCommandHandler.handle(new DeleteWorklogCommand(
                projectId,
                workItemId,
                worklogId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{worklogId}")
    public ResponseEntity<GeneralResponse<WorklogDetailView>> getWorklogById(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @PathVariable Long worklogId) {
        WorklogDetailView response = getWorklogByIdQueryHandler.handle(new GetWorklogByIdQuery(
                projectId,
                workItemId,
                worklogId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<WorklogListPageView>> listWorklogs(
            @PathVariable Long projectId,
            @PathVariable Long workItemId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        WorklogListPageView response = listWorklogsQueryHandler.handle(new ListWorklogsQuery(
                projectId,
                workItemId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups(),
                authorId,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    private Long requireCurrentUserId() {
        return authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
    }

    private Long requireCurrentTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));
    }
}
