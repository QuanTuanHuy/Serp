/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.status;

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
import serp.project.pmcore.application.status.StatusView;
import serp.project.pmcore.application.status.command.create.CreateStatusCommand;
import serp.project.pmcore.application.status.command.create.CreateStatusCommandHandler;
import serp.project.pmcore.application.status.command.delete.DeleteStatusCommand;
import serp.project.pmcore.application.status.command.delete.DeleteStatusCommandHandler;
import serp.project.pmcore.application.status.command.delete.DeleteStatusResult;
import serp.project.pmcore.application.status.command.update.UpdateStatusCommand;
import serp.project.pmcore.application.status.command.update.UpdateStatusCommandHandler;
import serp.project.pmcore.application.status.query.get.GetStatusByIdQuery;
import serp.project.pmcore.application.status.query.get.GetStatusByIdQueryHandler;
import serp.project.pmcore.application.status.query.list.ListStatusesQuery;
import serp.project.pmcore.application.status.query.list.ListStatusesQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.status.dto.request.CreateStatusRequest;
import serp.project.pmcore.ui.rest.status.dto.request.UpdateStatusRequest;

@RestController
@RequestMapping(PathConstants.STATUSES)
@RequiredArgsConstructor
public class StatusController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateStatusCommandHandler createStatusCommandHandler;
    private final UpdateStatusCommandHandler updateStatusCommandHandler;
    private final DeleteStatusCommandHandler deleteStatusCommandHandler;
    private final GetStatusByIdQueryHandler getStatusByIdQueryHandler;
    private final ListStatusesQueryHandler listStatusesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<StatusView>> createStatus(
            @Valid @RequestBody CreateStatusRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        StatusView response = createStatusCommandHandler.handle(new CreateStatusCommand(
                request.getStatusKey(),
                request.getName(),
                request.getDescription(),
                request.getIconUrl(),
                request.getStatusCategoryId(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<StatusView>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        StatusView response = updateStatusCommandHandler.handle(new UpdateStatusCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<StatusView>> getStatusById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        StatusView response = getStatusByIdQueryHandler.handle(new GetStatusByIdQuery(id, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<StatusView>>> listStatuses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long statusCategoryId,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<StatusView> response = listStatusesQueryHandler.handle(new ListStatusesQuery(
                tenantId,
                search,
                statusCategoryId,
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
    public ResponseEntity<GeneralResponse<DeleteStatusResult>> deleteStatus(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeleteStatusResult response = deleteStatusCommandHandler.handle(new DeleteStatusCommand(
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
