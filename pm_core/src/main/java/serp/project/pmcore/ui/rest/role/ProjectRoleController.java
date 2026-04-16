/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.role;

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
import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.role.command.create.CreateProjectRoleCommand;
import serp.project.pmcore.application.role.command.create.CreateProjectRoleCommandHandler;
import serp.project.pmcore.application.role.command.delete.DeleteProjectRoleCommand;
import serp.project.pmcore.application.role.command.delete.DeleteProjectRoleCommandHandler;
import serp.project.pmcore.application.role.command.delete.DeleteProjectRoleResult;
import serp.project.pmcore.application.role.command.update.UpdateProjectRoleCommand;
import serp.project.pmcore.application.role.command.update.UpdateProjectRoleCommandHandler;
import serp.project.pmcore.application.role.query.get.GetProjectRoleByIdQuery;
import serp.project.pmcore.application.role.query.get.GetProjectRoleByIdQueryHandler;
import serp.project.pmcore.application.role.query.list.ListProjectRoleQuery;
import serp.project.pmcore.application.role.query.list.ListProjectRoleQueryHandler;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.role.dto.request.CreateProjectRoleRequest;
import serp.project.pmcore.ui.rest.role.dto.request.UpdateProjectRoleRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.ROLES)
@RequiredArgsConstructor
public class ProjectRoleController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    private final CreateProjectRoleCommandHandler createProjectRoleCommandHandler;
    private final UpdateProjectRoleCommandHandler updateProjectRoleCommandHandler;
    private final DeleteProjectRoleCommandHandler deleteProjectRoleCommandHandler;
    private final GetProjectRoleByIdQueryHandler getProjectRoleByIdQueryHandler;
    private final ListProjectRoleQueryHandler listProjectRoleQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ProjectRoleView>> createProjectRole(
            @Valid @RequestBody CreateProjectRoleRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        ProjectRoleView role = createProjectRoleCommandHandler.handle(new CreateProjectRoleCommand(
                request.getName(),
                request.getDescription(),
                tenantId,
                userId
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectRoleView>> updateProjectRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRoleRequest request) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();

        ProjectRoleView role = updateProjectRoleCommandHandler.handle(new UpdateProjectRoleCommand(
                id,
                request.toData(),
                tenantId,
                userId
        ));
        return ResponseEntity.ok(responseUtils.success(role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectRoleView>> getProjectRoleById(@PathVariable Long id) {
        ProjectRoleView role = getProjectRoleByIdQueryHandler.handle(new GetProjectRoleByIdQuery(id, requireCurrentTenantId()));
        return ResponseEntity.ok(responseUtils.success(role));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<ProjectRoleView>>> getProjectRoles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isSystem,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        PageView<ProjectRoleView> roles = listProjectRoleQueryHandler.handle(
                new ListProjectRoleQuery(
                        requireCurrentTenantId(),
                        search,
                        isSystem,
                        page,
                        pageSize,
                        sortBy,
                        sortDirection
                )
        );
        return ResponseEntity.ok(responseUtils.success(roles));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<DeleteProjectRoleResult>> deleteProjectRole(@PathVariable Long id) {
        Long userId = requireCurrentUserId();
        Long tenantId = requireCurrentTenantId();
        DeleteProjectRoleResult result = deleteProjectRoleCommandHandler.handle(new DeleteProjectRoleCommand(
                id,
                tenantId,
                userId
        ));
        return ResponseEntity.ok(responseUtils.success(result));
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
