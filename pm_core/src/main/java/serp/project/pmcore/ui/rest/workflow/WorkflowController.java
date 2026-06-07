/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.workflow;

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
import serp.project.pmcore.application.workflow.WorkflowView;
import serp.project.pmcore.application.workflow.WorkflowValidationView;
import serp.project.pmcore.application.workflow.WorkflowStepView;
import serp.project.pmcore.application.workflow.WorkflowTransitionView;
import serp.project.pmcore.application.workflow.command.addstep.AddWorkflowStepCommand;
import serp.project.pmcore.application.workflow.command.addstep.AddWorkflowStepCommandHandler;
import serp.project.pmcore.application.workflow.command.addtransition.AddWorkflowTransitionCommand;
import serp.project.pmcore.application.workflow.command.addtransition.AddWorkflowTransitionCommandHandler;
import serp.project.pmcore.application.workflow.command.create.CreateWorkflowCommand;
import serp.project.pmcore.application.workflow.command.create.CreateWorkflowCommandHandler;
import serp.project.pmcore.application.workflow.command.publish.PublishWorkflowCommand;
import serp.project.pmcore.application.workflow.command.publish.PublishWorkflowCommandHandler;
import serp.project.pmcore.application.workflow.command.reordersteps.ReorderWorkflowStepsCommand;
import serp.project.pmcore.application.workflow.command.reordersteps.ReorderWorkflowStepsCommandHandler;
import serp.project.pmcore.application.workflow.command.removetransition.DeleteWorkflowTransitionResult;
import serp.project.pmcore.application.workflow.command.removetransition.RemoveWorkflowTransitionCommand;
import serp.project.pmcore.application.workflow.command.removetransition.RemoveWorkflowTransitionCommandHandler;
import serp.project.pmcore.application.workflow.command.removestep.DeleteWorkflowStepResult;
import serp.project.pmcore.application.workflow.command.removestep.RemoveWorkflowStepCommand;
import serp.project.pmcore.application.workflow.command.removestep.RemoveWorkflowStepCommandHandler;
import serp.project.pmcore.application.workflow.command.update.UpdateWorkflowCommand;
import serp.project.pmcore.application.workflow.command.update.UpdateWorkflowCommandHandler;
import serp.project.pmcore.application.workflow.command.updatetransition.UpdateWorkflowTransitionCommand;
import serp.project.pmcore.application.workflow.command.updatetransition.UpdateWorkflowTransitionCommandHandler;
import serp.project.pmcore.application.workflow.query.get.GetWorkflowByIdQuery;
import serp.project.pmcore.application.workflow.query.get.GetWorkflowByIdQueryHandler;
import serp.project.pmcore.application.workflow.query.editor.GetWorkflowEditorQuery;
import serp.project.pmcore.application.workflow.query.editor.GetWorkflowEditorQueryHandler;
import serp.project.pmcore.application.workflow.query.editor.WorkflowEditorView;
import serp.project.pmcore.application.workflow.query.list.ListWorkflowsQuery;
import serp.project.pmcore.application.workflow.query.list.ListWorkflowsQueryHandler;
import serp.project.pmcore.application.workflow.query.listtransitions.ListWorkflowTransitionsQuery;
import serp.project.pmcore.application.workflow.query.listtransitions.ListWorkflowTransitionsQueryHandler;
import serp.project.pmcore.application.workflow.query.validate.ValidateWorkflowQuery;
import serp.project.pmcore.application.workflow.query.validate.ValidateWorkflowQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.workflow.dto.request.AddWorkflowStepRequest;
import serp.project.pmcore.ui.rest.workflow.dto.request.AddWorkflowTransitionRequest;
import serp.project.pmcore.ui.rest.workflow.dto.request.CreateWorkflowRequest;
import serp.project.pmcore.ui.rest.workflow.dto.request.ReorderWorkflowStepsRequest;
import serp.project.pmcore.ui.rest.workflow.dto.request.UpdateWorkflowRequest;
import serp.project.pmcore.ui.rest.workflow.dto.request.UpdateWorkflowTransitionRequest;

import java.util.List;

@RestController
@RequestMapping(PathConstants.WORKFLOWS)
@RequiredArgsConstructor
public class WorkflowController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateWorkflowCommandHandler createWorkflowCommandHandler;
    private final UpdateWorkflowCommandHandler updateWorkflowCommandHandler;
    private final GetWorkflowByIdQueryHandler getWorkflowByIdQueryHandler;
    private final GetWorkflowEditorQueryHandler getWorkflowEditorQueryHandler;
    private final ListWorkflowsQueryHandler listWorkflowsQueryHandler;
    private final AddWorkflowStepCommandHandler addWorkflowStepCommandHandler;
    private final RemoveWorkflowStepCommandHandler removeWorkflowStepCommandHandler;
    private final ReorderWorkflowStepsCommandHandler reorderWorkflowStepsCommandHandler;
    private final AddWorkflowTransitionCommandHandler addWorkflowTransitionCommandHandler;
    private final UpdateWorkflowTransitionCommandHandler updateWorkflowTransitionCommandHandler;
    private final RemoveWorkflowTransitionCommandHandler removeWorkflowTransitionCommandHandler;
    private final ListWorkflowTransitionsQueryHandler listWorkflowTransitionsQueryHandler;
    private final ValidateWorkflowQueryHandler validateWorkflowQueryHandler;
    private final PublishWorkflowCommandHandler publishWorkflowCommandHandler;

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

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<WorkflowView>> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWorkflowRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowView response = updateWorkflowCommandHandler.handle(new UpdateWorkflowCommand(
                id,
                request.getName(),
                request.getDescription(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<WorkflowView>> getWorkflowById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        WorkflowView response = getWorkflowByIdQueryHandler.handle(new GetWorkflowByIdQuery(id, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}/editor")
    public ResponseEntity<GeneralResponse<WorkflowEditorView>> getWorkflowEditor(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        WorkflowEditorView response = getWorkflowEditorQueryHandler.handle(new GetWorkflowEditorQuery(id, tenantId));
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

    @PostMapping("/{workflowId}/steps")
    public ResponseEntity<GeneralResponse<WorkflowStepView>> addWorkflowStep(
            @PathVariable Long workflowId,
            @Valid @RequestBody AddWorkflowStepRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowStepView response = addWorkflowStepCommandHandler.handle(new AddWorkflowStepCommand(
                workflowId,
                request.getStatusId(),
                request.getIsInitial(),
                request.getIsTerminal(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @DeleteMapping("/{workflowId}/steps/{stepId}")
    public ResponseEntity<GeneralResponse<DeleteWorkflowStepResult>> removeWorkflowStep(
            @PathVariable Long workflowId,
            @PathVariable Long stepId) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        DeleteWorkflowStepResult response = removeWorkflowStepCommandHandler.handle(new RemoveWorkflowStepCommand(
                workflowId,
                stepId,
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PutMapping("/{workflowId}/steps/reorder")
    public ResponseEntity<GeneralResponse<List<WorkflowStepView>>> reorderWorkflowSteps(
            @PathVariable Long workflowId,
            @Valid @RequestBody ReorderWorkflowStepsRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        List<WorkflowStepView> response = reorderWorkflowStepsCommandHandler.handle(new ReorderWorkflowStepsCommand(
                workflowId,
                request.getStepIds(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{workflowId}/transitions")
    public ResponseEntity<GeneralResponse<WorkflowTransitionView>> addWorkflowTransition(
            @PathVariable Long workflowId,
            @Valid @RequestBody AddWorkflowTransitionRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowTransitionView response = addWorkflowTransitionCommandHandler.handle(new AddWorkflowTransitionCommand(
                workflowId,
                request.getName(),
                request.getFromStepId(),
                request.getToStepId(),
                request.getScreenId(),
                request.getSequence(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{workflowId}/transitions/{transitionId}")
    public ResponseEntity<GeneralResponse<WorkflowTransitionView>> updateWorkflowTransition(
            @PathVariable Long workflowId,
            @PathVariable Long transitionId,
            @Valid @RequestBody UpdateWorkflowTransitionRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        WorkflowTransitionView response = updateWorkflowTransitionCommandHandler.handle(new UpdateWorkflowTransitionCommand(
                workflowId,
                transitionId,
                request.getName(),
                request.getScreenId(),
                request.getSequence(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{workflowId}/transitions/{transitionId}")
    public ResponseEntity<GeneralResponse<DeleteWorkflowTransitionResult>> removeWorkflowTransition(
            @PathVariable Long workflowId,
            @PathVariable Long transitionId) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        DeleteWorkflowTransitionResult response = removeWorkflowTransitionCommandHandler.handle(
                new RemoveWorkflowTransitionCommand(
                        workflowId,
                        transitionId,
                        tenantId,
                        userId
                )
        );

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{workflowId}/transitions")
    public ResponseEntity<GeneralResponse<List<WorkflowTransitionView>>> listWorkflowTransitions(
            @PathVariable Long workflowId,
            @RequestParam(required = false) Long fromStepId) {
        Long tenantId = requireCurrentTenantId();
        List<WorkflowTransitionView> response = listWorkflowTransitionsQueryHandler.handle(
                new ListWorkflowTransitionsQuery(workflowId, fromStepId, tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{workflowId}/validate")
    public ResponseEntity<GeneralResponse<WorkflowValidationView>> validateWorkflow(@PathVariable Long workflowId) {
        Long tenantId = requireCurrentTenantId();
        WorkflowValidationView response = validateWorkflowQueryHandler.handle(new ValidateWorkflowQuery(workflowId, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{workflowId}/publish")
    public ResponseEntity<GeneralResponse<WorkflowView>> publishWorkflow(@PathVariable Long workflowId) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        WorkflowView response = publishWorkflowCommandHandler.handle(new PublishWorkflowCommand(
                workflowId,
                tenantId,
                userId
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
