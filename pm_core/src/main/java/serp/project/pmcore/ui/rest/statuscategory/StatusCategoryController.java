/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.statuscategory;

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
import serp.project.pmcore.application.statuscategory.StatusCategoryView;
import serp.project.pmcore.application.statuscategory.command.create.CreateStatusCategoryCommand;
import serp.project.pmcore.application.statuscategory.command.create.CreateStatusCategoryCommandHandler;
import serp.project.pmcore.application.statuscategory.command.delete.DeleteStatusCategoryCommand;
import serp.project.pmcore.application.statuscategory.command.delete.DeleteStatusCategoryCommandHandler;
import serp.project.pmcore.application.statuscategory.command.delete.DeleteStatusCategoryResult;
import serp.project.pmcore.application.statuscategory.command.update.UpdateStatusCategoryCommand;
import serp.project.pmcore.application.statuscategory.command.update.UpdateStatusCategoryCommandHandler;
import serp.project.pmcore.application.statuscategory.query.get.GetStatusCategoryByIdQuery;
import serp.project.pmcore.application.statuscategory.query.get.GetStatusCategoryByIdQueryHandler;
import serp.project.pmcore.application.statuscategory.query.list.ListStatusCategoriesQuery;
import serp.project.pmcore.application.statuscategory.query.list.ListStatusCategoriesQueryHandler;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;
import serp.project.pmcore.ui.rest.statuscategory.dto.request.CreateStatusCategoryRequest;
import serp.project.pmcore.ui.rest.statuscategory.dto.request.UpdateStatusCategoryRequest;

@RestController
@RequestMapping(PathConstants.STATUS_CATEGORIES)
@RequiredArgsConstructor
public class StatusCategoryController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateStatusCategoryCommandHandler createStatusCategoryCommandHandler;
    private final UpdateStatusCategoryCommandHandler updateStatusCategoryCommandHandler;
    private final DeleteStatusCategoryCommandHandler deleteStatusCategoryCommandHandler;
    private final GetStatusCategoryByIdQueryHandler getStatusCategoryByIdQueryHandler;
    private final ListStatusCategoriesQueryHandler listStatusCategoriesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<StatusCategoryView>> createStatusCategory(
            @Valid @RequestBody CreateStatusCategoryRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        StatusCategoryView response = createStatusCategoryCommandHandler.handle(new CreateStatusCategoryCommand(
                request.getName(),
                request.getKey(),
                request.getColor(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<StatusCategoryView>> updateStatusCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusCategoryRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        StatusCategoryView response = updateStatusCategoryCommandHandler.handle(new UpdateStatusCategoryCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<StatusCategoryView>> getStatusCategoryById(@PathVariable Long id) {
        Long tenantId = requireCurrentTenantId();
        StatusCategoryView response = getStatusCategoryByIdQueryHandler.handle(
                new GetStatusCategoryByIdQuery(id, tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<StatusCategoryView>>> listStatusCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long tenantId = requireCurrentTenantId();
        PageView<StatusCategoryView> response = listStatusCategoriesQueryHandler.handle(new ListStatusCategoriesQuery(
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

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteStatusCategoryResult>> deleteStatusCategory(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeleteStatusCategoryResult response = deleteStatusCategoryCommandHandler.handle(new DeleteStatusCategoryCommand(
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
