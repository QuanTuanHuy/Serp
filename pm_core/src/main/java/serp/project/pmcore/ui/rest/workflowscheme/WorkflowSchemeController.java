/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workflowscheme;

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
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeDetailView;
import serp.project.pmcore.application.workflowscheme.WorkflowSchemeView;
import serp.project.pmcore.application.workflowscheme.command.create.CreateWorkflowSchemeCommand;
import serp.project.pmcore.application.workflowscheme.command.create.CreateWorkflowSchemeCommandHandler;
import serp.project.pmcore.application.workflowscheme.command.delete.DeleteWorkflowSchemeCommand;
import serp.project.pmcore.application.workflowscheme.command.delete.DeleteWorkflowSchemeCommandHandler;
import serp.project.pmcore.application.workflowscheme.command.delete.DeleteWorkflowSchemeResult;
import serp.project.pmcore.application.workflowscheme.command.manageitems.ManageWorkflowSchemeItemsCommand;
import serp.project.pmcore.application.workflowscheme.command.manageitems.ManageWorkflowSchemeItemsCommandHandler;
import serp.project.pmcore.application.workflowscheme.command.update.UpdateWorkflowSchemeCommand;
import serp.project.pmcore.application.workflowscheme.command.update.UpdateWorkflowSchemeCommandHandler;
import serp.project.pmcore.application.workflowscheme.query.get.GetWorkflowSchemeByIdQuery;
import serp.project.pmcore.application.workflowscheme.query.get.GetWorkflowSchemeByIdQueryHandler;
import serp.project.pmcore.application.workflowscheme.query.list.ListWorkflowSchemesQuery;
import serp.project.pmcore.application.workflowscheme.query.list.ListWorkflowSchemesQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.workflowscheme.dto.request.CreateWorkflowSchemeRequest;
import serp.project.pmcore.ui.rest.workflowscheme.dto.request.ManageWorkflowSchemeItemsRequest;
import serp.project.pmcore.ui.rest.workflowscheme.dto.request.UpdateWorkflowSchemeRequest;

@RestController
@RequestMapping(PathConstants.WORKFLOW_SCHEMES)
@RequiredArgsConstructor
public class WorkflowSchemeController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateWorkflowSchemeCommandHandler createWorkflowSchemeCommandHandler;
    private final UpdateWorkflowSchemeCommandHandler updateWorkflowSchemeCommandHandler;
    private final DeleteWorkflowSchemeCommandHandler deleteWorkflowSchemeCommandHandler;
    private final ManageWorkflowSchemeItemsCommandHandler manageWorkflowSchemeItemsCommandHandler;
    private final GetWorkflowSchemeByIdQueryHandler getWorkflowSchemeByIdQueryHandler;
    private final ListWorkflowSchemesQueryHandler listWorkflowSchemesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<WorkflowSchemeView>> createWorkflowScheme(
            @Valid @RequestBody CreateWorkflowSchemeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowSchemeView response = createWorkflowSchemeCommandHandler.handle(new CreateWorkflowSchemeCommand(
                request.getName(),
                request.getDescription(),
                request.getDefaultWorkflowId(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<WorkflowSchemeView>> updateWorkflowScheme(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkflowSchemeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowSchemeView response = updateWorkflowSchemeCommandHandler.handle(new UpdateWorkflowSchemeCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<WorkflowSchemeDetailView>> getWorkflowSchemeById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        WorkflowSchemeDetailView response = getWorkflowSchemeByIdQueryHandler.handle(
                new GetWorkflowSchemeByIdQuery(id, tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<WorkflowSchemeView>>> listWorkflowSchemes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<WorkflowSchemeView> response = listWorkflowSchemesQueryHandler.handle(new ListWorkflowSchemesQuery(
                tenantId,
                search,
                isSystem,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PutMapping("/{id}/items")
    public ResponseEntity<GeneralResponse<WorkflowSchemeDetailView>> manageWorkflowSchemeItems(
            @PathVariable Long id,
            @Valid @RequestBody ManageWorkflowSchemeItemsRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowSchemeDetailView response = manageWorkflowSchemeItemsCommandHandler.handle(
                new ManageWorkflowSchemeItemsCommand(
                        id,
                        request.getItems().stream().map(ManageWorkflowSchemeItemsRequest.WorkflowSchemeItemRequest::toInput).toList(),
                        tenantId,
                        userId
                )
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteWorkflowSchemeResult>> deleteWorkflowScheme(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeleteWorkflowSchemeResult response = deleteWorkflowSchemeCommandHandler.handle(
                new DeleteWorkflowSchemeCommand(id, tenantId, userId)
        );
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
