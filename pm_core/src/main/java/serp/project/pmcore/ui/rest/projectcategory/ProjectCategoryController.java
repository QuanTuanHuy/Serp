/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.projectcategory;

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
import serp.project.pmcore.application.projectcategory.ProjectCategoryView;
import serp.project.pmcore.application.projectcategory.command.create.CreateProjectCategoryCommand;
import serp.project.pmcore.application.projectcategory.command.create.CreateProjectCategoryCommandHandler;
import serp.project.pmcore.application.projectcategory.command.delete.DeleteProjectCategoryCommand;
import serp.project.pmcore.application.projectcategory.command.delete.DeleteProjectCategoryCommandHandler;
import serp.project.pmcore.application.projectcategory.command.delete.DeleteProjectCategoryResult;
import serp.project.pmcore.application.projectcategory.command.update.UpdateProjectCategoryCommand;
import serp.project.pmcore.application.projectcategory.command.update.UpdateProjectCategoryCommandHandler;
import serp.project.pmcore.application.projectcategory.query.get.GetProjectCategoryByIdQuery;
import serp.project.pmcore.application.projectcategory.query.get.GetProjectCategoryByIdQueryHandler;
import serp.project.pmcore.application.projectcategory.query.list.ListProjectCategoriesQuery;
import serp.project.pmcore.application.projectcategory.query.list.ListProjectCategoriesQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.projectcategory.dto.request.CreateProjectCategoryRequest;
import serp.project.pmcore.ui.rest.projectcategory.dto.request.UpdateProjectCategoryRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PROJECT_CATEGORIES)
@RequiredArgsConstructor
public class ProjectCategoryController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateProjectCategoryCommandHandler createProjectCategoryCommandHandler;
    private final UpdateProjectCategoryCommandHandler updateProjectCategoryCommandHandler;
    private final DeleteProjectCategoryCommandHandler deleteProjectCategoryCommandHandler;
    private final GetProjectCategoryByIdQueryHandler getProjectCategoryByIdQueryHandler;
    private final ListProjectCategoriesQueryHandler listProjectCategoriesQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ProjectCategoryView>> createProjectCategory(
            @Valid @RequestBody CreateProjectCategoryRequest request) {
        ProjectCategoryView response = createProjectCategoryCommandHandler.handle(new CreateProjectCategoryCommand(
                request.getName(),
                request.getDescription(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectCategoryView>> updateProjectCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectCategoryRequest request) {
        ProjectCategoryView response = updateProjectCategoryCommandHandler.handle(new UpdateProjectCategoryCommand(
                id,
                request.toData(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectCategoryView>> getProjectCategoryById(@PathVariable Long id) {
        ProjectCategoryView response = getProjectCategoryByIdQueryHandler.handle(
                new GetProjectCategoryByIdQuery(id, requireCurrentTenantId())
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<ProjectCategoryView>>> listProjectCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageView<ProjectCategoryView> response = listProjectCategoriesQueryHandler.handle(new ListProjectCategoriesQuery(
                requireCurrentTenantId(),
                search,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteProjectCategoryResult>> deleteProjectCategory(@PathVariable Long id) {
        DeleteProjectCategoryResult response = deleteProjectCategoryCommandHandler.handle(new DeleteProjectCategoryCommand(
                id,
                requireCurrentTenantId(),
                requireCurrentUserId()
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
