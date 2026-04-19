/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.blueprint;

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
import serp.project.pmcore.application.blueprint.ProjectBlueprintDetailView;
import serp.project.pmcore.application.blueprint.ProjectBlueprintView;
import serp.project.pmcore.application.blueprint.command.create.CreateProjectBlueprintCommand;
import serp.project.pmcore.application.blueprint.command.create.CreateProjectBlueprintCommandHandler;
import serp.project.pmcore.application.blueprint.command.delete.DeleteProjectBlueprintCommand;
import serp.project.pmcore.application.blueprint.command.delete.DeleteProjectBlueprintCommandHandler;
import serp.project.pmcore.application.blueprint.command.delete.DeleteProjectBlueprintResult;
import serp.project.pmcore.application.blueprint.command.update.UpdateProjectBlueprintCommand;
import serp.project.pmcore.application.blueprint.command.update.UpdateProjectBlueprintCommandHandler;
import serp.project.pmcore.application.blueprint.query.get.GetProjectBlueprintByIdQuery;
import serp.project.pmcore.application.blueprint.query.get.GetProjectBlueprintByIdQueryHandler;
import serp.project.pmcore.application.blueprint.query.list.ListProjectBlueprintsQuery;
import serp.project.pmcore.application.blueprint.query.list.ListProjectBlueprintsQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.blueprint.dto.request.CreateProjectBlueprintRequest;
import serp.project.pmcore.ui.rest.blueprint.dto.request.UpdateProjectBlueprintRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PROJECT_BLUEPRINTS)
@RequiredArgsConstructor
public class ProjectBlueprintController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateProjectBlueprintCommandHandler createProjectBlueprintCommandHandler;
    private final UpdateProjectBlueprintCommandHandler updateProjectBlueprintCommandHandler;
    private final DeleteProjectBlueprintCommandHandler deleteProjectBlueprintCommandHandler;
    private final GetProjectBlueprintByIdQueryHandler getProjectBlueprintByIdQueryHandler;
    private final ListProjectBlueprintsQueryHandler listProjectBlueprintsQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ProjectBlueprintView>> createProjectBlueprint(
            @Valid @RequestBody CreateProjectBlueprintRequest request) {
        ProjectBlueprintView response = createProjectBlueprintCommandHandler.handle(new CreateProjectBlueprintCommand(
                request.getName(),
                request.getDescription(),
                request.getProjectTypeKey(),
                request.getAvatarUrl(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectBlueprintView>> updateProjectBlueprint(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectBlueprintRequest request) {
        ProjectBlueprintView response = updateProjectBlueprintCommandHandler.handle(new UpdateProjectBlueprintCommand(
                id,
                request.toData(),
                requireCurrentTenantId(),
                requireCurrentUserId()
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectBlueprintDetailView>> getProjectBlueprintById(@PathVariable Long id) {
        ProjectBlueprintDetailView response = getProjectBlueprintByIdQueryHandler.handle(
                new GetProjectBlueprintByIdQuery(id, requireCurrentTenantId())
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<ProjectBlueprintView>>> listProjectBlueprints(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String projectTypeKey,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageView<ProjectBlueprintView> response = listProjectBlueprintsQueryHandler.handle(new ListProjectBlueprintsQuery(
                requireCurrentTenantId(),
                search,
                projectTypeKey,
                isSystem,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteProjectBlueprintResult>> deleteProjectBlueprint(@PathVariable Long id) {
        DeleteProjectBlueprintResult response = deleteProjectBlueprintCommandHandler.handle(new DeleteProjectBlueprintCommand(
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
