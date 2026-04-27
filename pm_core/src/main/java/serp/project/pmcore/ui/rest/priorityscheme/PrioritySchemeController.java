/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.priorityscheme;

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
import serp.project.pmcore.application.priorityscheme.PrioritySchemeDetailView;
import serp.project.pmcore.application.priorityscheme.PrioritySchemeView;
import serp.project.pmcore.application.priorityscheme.command.create.CreatePrioritySchemeCommand;
import serp.project.pmcore.application.priorityscheme.command.create.CreatePrioritySchemeCommandHandler;
import serp.project.pmcore.application.priorityscheme.command.delete.DeletePrioritySchemeCommand;
import serp.project.pmcore.application.priorityscheme.command.delete.DeletePrioritySchemeCommandHandler;
import serp.project.pmcore.application.priorityscheme.command.delete.DeletePrioritySchemeResult;
import serp.project.pmcore.application.priorityscheme.command.manageitems.ManagePrioritySchemeItemsCommand;
import serp.project.pmcore.application.priorityscheme.command.manageitems.ManagePrioritySchemeItemsCommandHandler;
import serp.project.pmcore.application.priorityscheme.command.update.UpdatePrioritySchemeCommand;
import serp.project.pmcore.application.priorityscheme.command.update.UpdatePrioritySchemeCommandHandler;
import serp.project.pmcore.application.priorityscheme.query.get.GetPrioritySchemeByIdQuery;
import serp.project.pmcore.application.priorityscheme.query.get.GetPrioritySchemeByIdQueryHandler;
import serp.project.pmcore.application.priorityscheme.query.list.ListPrioritySchemesQuery;
import serp.project.pmcore.application.priorityscheme.query.list.ListPrioritySchemesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.priorityscheme.dto.request.CreatePrioritySchemeRequest;
import serp.project.pmcore.ui.rest.priorityscheme.dto.request.ManagePrioritySchemeItemsRequest;
import serp.project.pmcore.ui.rest.priorityscheme.dto.request.UpdatePrioritySchemeRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PRIORITY_SCHEMES)
@RequiredArgsConstructor
public class PrioritySchemeController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreatePrioritySchemeCommandHandler createPrioritySchemeCommandHandler;
    private final UpdatePrioritySchemeCommandHandler updatePrioritySchemeCommandHandler;
    private final DeletePrioritySchemeCommandHandler deletePrioritySchemeCommandHandler;
    private final ManagePrioritySchemeItemsCommandHandler managePrioritySchemeItemsCommandHandler;
    private final GetPrioritySchemeByIdQueryHandler getPrioritySchemeByIdQueryHandler;
    private final ListPrioritySchemesQueryHandler listPrioritySchemesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<PrioritySchemeView>> createPriorityScheme(
            @Valid @RequestBody CreatePrioritySchemeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        PrioritySchemeView response = createPrioritySchemeCommandHandler.handle(new CreatePrioritySchemeCommand(
                request.getName(),
                request.getDescription(),
                request.getDefaultPriorityId(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<PrioritySchemeView>> updatePriorityScheme(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePrioritySchemeRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        PrioritySchemeView response = updatePrioritySchemeCommandHandler.handle(new UpdatePrioritySchemeCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<PrioritySchemeDetailView>> getPrioritySchemeById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        PrioritySchemeDetailView response = getPrioritySchemeByIdQueryHandler.handle(
                new GetPrioritySchemeByIdQuery(id, tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<PrioritySchemeView>>> listPrioritySchemes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<PrioritySchemeView> response = listPrioritySchemesQueryHandler.handle(new ListPrioritySchemesQuery(
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
    public ResponseEntity<GeneralResponse<PrioritySchemeDetailView>> managePrioritySchemeItems(
            @PathVariable Long id,
            @Valid @RequestBody ManagePrioritySchemeItemsRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        PrioritySchemeDetailView response = managePrioritySchemeItemsCommandHandler.handle(
                new ManagePrioritySchemeItemsCommand(id, request.getPriorityIds(), tenantId, userId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeletePrioritySchemeResult>> deletePriorityScheme(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeletePrioritySchemeResult response = deletePrioritySchemeCommandHandler.handle(
                new DeletePrioritySchemeCommand(id, tenantId, userId)
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
