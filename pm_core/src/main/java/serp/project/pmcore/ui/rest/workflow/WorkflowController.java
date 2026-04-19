/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workflow;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.application.workflow.command.create.CreateWorkflowCommand;
import serp.project.pmcore.application.workflow.command.create.CreateWorkflowCommandHandler;
import serp.project.pmcore.application.workflow.query.get.GetWorkflowByIdQuery;
import serp.project.pmcore.application.workflow.query.get.GetWorkflowByIdQueryHandler;
import serp.project.pmcore.application.workflow.query.list.ListWorkflowsQuery;
import serp.project.pmcore.application.workflow.query.list.ListWorkflowsQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.workflow.dto.request.CreateWorkflowRequest;

@RestController
@RequestMapping(PathConstants.WORKFLOWS)
@RequiredArgsConstructor
public class WorkflowController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateWorkflowCommandHandler createWorkflowCommandHandler;
    private final GetWorkflowByIdQueryHandler getWorkflowByIdQueryHandler;
    private final ListWorkflowsQueryHandler listWorkflowsQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<WorkflowView>> createWorkflow(
            @Valid @RequestBody CreateWorkflowRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowView response = createWorkflowCommandHandler.handle(new CreateWorkflowCommand(
                request.getName(),
                request.getDescription(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<WorkflowView>> getWorkflowById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        WorkflowView response = getWorkflowByIdQueryHandler.handle(new GetWorkflowByIdQuery(id, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<WorkflowView>>> listWorkflows(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<WorkflowView> response = listWorkflowsQueryHandler.handle(new ListWorkflowsQuery(
                tenantId,
                search,
                isActive,
                isSystem,
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
