/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.priority;

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
import serp.project.pmcore.application.priority.PriorityView;
import serp.project.pmcore.application.priority.command.create.CreatePriorityCommand;
import serp.project.pmcore.application.priority.command.create.CreatePriorityCommandHandler;
import serp.project.pmcore.application.priority.command.delete.DeletePriorityCommand;
import serp.project.pmcore.application.priority.command.delete.DeletePriorityCommandHandler;
import serp.project.pmcore.application.priority.command.delete.DeletePriorityResult;
import serp.project.pmcore.application.priority.command.update.UpdatePriorityCommand;
import serp.project.pmcore.application.priority.command.update.UpdatePriorityCommandHandler;
import serp.project.pmcore.application.priority.query.get.GetPriorityByIdQuery;
import serp.project.pmcore.application.priority.query.get.GetPriorityByIdQueryHandler;
import serp.project.pmcore.application.priority.query.list.ListPrioritiesQuery;
import serp.project.pmcore.application.priority.query.list.ListPrioritiesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.priority.dto.request.CreatePriorityRequest;
import serp.project.pmcore.ui.rest.priority.dto.request.UpdatePriorityRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PRIORITIES)
@RequiredArgsConstructor
public class PriorityController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreatePriorityCommandHandler createPriorityCommandHandler;
    private final UpdatePriorityCommandHandler updatePriorityCommandHandler;
    private final DeletePriorityCommandHandler deletePriorityCommandHandler;
    private final GetPriorityByIdQueryHandler getPriorityByIdQueryHandler;
    private final ListPrioritiesQueryHandler listPrioritiesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<PriorityView>> createPriority(
            @Valid @RequestBody CreatePriorityRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        PriorityView response = createPriorityCommandHandler.handle(new CreatePriorityCommand(
                request.getName(),
                request.getDescription(),
                request.getIconUrl(),
                request.getColor(),
                request.getSequence(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<PriorityView>> updatePriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriorityRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        PriorityView response = updatePriorityCommandHandler.handle(new UpdatePriorityCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<PriorityView>> getPriorityById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        PriorityView response = getPriorityByIdQueryHandler.handle(new GetPriorityByIdQuery(id, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<PriorityView>>> listPriorities(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<PriorityView> response = listPrioritiesQueryHandler.handle(new ListPrioritiesQuery(
                tenantId,
                search,
                isSystem,
                projectId,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeletePriorityResult>> deletePriority(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeletePriorityResult response = deletePriorityCommandHandler.handle(new DeletePriorityCommand(
                id,
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
