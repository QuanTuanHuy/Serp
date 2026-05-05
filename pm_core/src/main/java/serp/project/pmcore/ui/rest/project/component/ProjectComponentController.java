/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.component;

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
import serp.project.pmcore.application.project.component.ProjectComponentView;
import serp.project.pmcore.application.project.component.command.create.CreateProjectComponentCommand;
import serp.project.pmcore.application.project.component.command.create.CreateProjectComponentCommandHandler;
import serp.project.pmcore.application.project.component.command.delete.DeleteProjectComponentCommand;
import serp.project.pmcore.application.project.component.command.delete.DeleteProjectComponentCommandHandler;
import serp.project.pmcore.application.project.component.command.delete.DeleteProjectComponentResult;
import serp.project.pmcore.application.project.component.command.update.UpdateProjectComponentCommand;
import serp.project.pmcore.application.project.component.command.update.UpdateProjectComponentCommandHandler;
import serp.project.pmcore.application.project.component.query.get.GetProjectComponentByIdQuery;
import serp.project.pmcore.application.project.component.query.get.GetProjectComponentByIdQueryHandler;
import serp.project.pmcore.application.project.component.query.list.ListProjectComponentsQuery;
import serp.project.pmcore.application.project.component.query.list.ListProjectComponentsQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.project.component.dto.request.CreateProjectComponentRequest;
import serp.project.pmcore.ui.rest.project.component.dto.request.UpdateProjectComponentRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PROJECT_COMPONENTS)
@RequiredArgsConstructor
public class ProjectComponentController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateProjectComponentCommandHandler createProjectComponentCommandHandler;
    private final UpdateProjectComponentCommandHandler updateProjectComponentCommandHandler;
    private final DeleteProjectComponentCommandHandler deleteProjectComponentCommandHandler;
    private final GetProjectComponentByIdQueryHandler getProjectComponentByIdQueryHandler;
    private final ListProjectComponentsQueryHandler listProjectComponentsQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ProjectComponentView>> createProjectComponent(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectComponentRequest request) {
        ProjectComponentView response = createProjectComponentCommandHandler.handle(new CreateProjectComponentCommand(
                projectId,
                request.getName(),
                request.getDescription(),
                request.getLeadUserId(),
                request.getAssigneeType(),
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{componentId}")
    public ResponseEntity<GeneralResponse<ProjectComponentView>> updateProjectComponent(
            @PathVariable Long projectId,
            @PathVariable Long componentId,
            @Valid @RequestBody UpdateProjectComponentRequest request) {
        ProjectComponentView response = updateProjectComponentCommandHandler.handle(new UpdateProjectComponentCommand(
                projectId,
                componentId,
                request.toData(),
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{componentId}")
    public ResponseEntity<GeneralResponse<ProjectComponentView>> getProjectComponentById(
            @PathVariable Long projectId,
            @PathVariable Long componentId) {
        ProjectComponentView response = getProjectComponentByIdQueryHandler.handle(new GetProjectComponentByIdQuery(
                projectId,
                componentId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<ProjectComponentView>>> listProjectComponents(
            @PathVariable Long projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageView<ProjectComponentView> response = listProjectComponentsQueryHandler.handle(new ListProjectComponentsQuery(
                projectId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups(),
                search,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{componentId}")
    public ResponseEntity<GeneralResponse<DeleteProjectComponentResult>> deleteProjectComponent(
            @PathVariable Long projectId,
            @PathVariable Long componentId) {
        DeleteProjectComponentResult response = deleteProjectComponentCommandHandler.handle(new DeleteProjectComponentCommand(
                projectId,
                componentId,
                requireCurrentTenantId(),
                requireCurrentUserId(),
                authUtils.getCurrentGroups()
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
