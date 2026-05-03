/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.project.command.create.CreateProjectCommand;
import serp.project.pmcore.application.project.command.create.CreateProjectCommandHandler;
import serp.project.pmcore.application.project.command.create.CreateProjectResult;
import serp.project.pmcore.application.project.command.archive.ArchiveProjectCommand;
import serp.project.pmcore.application.project.command.archive.ArchiveProjectCommandHandler;
import serp.project.pmcore.application.project.command.unarchive.UnarchiveProjectCommand;
import serp.project.pmcore.application.project.command.unarchive.UnarchiveProjectCommandHandler;
import serp.project.pmcore.application.project.command.update.UpdateProjectCommand;
import serp.project.pmcore.application.project.command.update.UpdateProjectCommandHandler;
import serp.project.pmcore.application.project.command.update.UpdateProjectResult;
import serp.project.pmcore.application.project.query.get.GetProjectByIdQuery;
import serp.project.pmcore.application.project.query.get.GetProjectByIdQueryHandler;
import serp.project.pmcore.application.project.query.get.GetProjectByKeyQuery;
import serp.project.pmcore.application.project.query.get.GetProjectByKeyQueryHandler;
import serp.project.pmcore.application.project.query.get.ProjectDetailView;
import serp.project.pmcore.application.project.query.list.ListProjectsQuery;
import serp.project.pmcore.application.project.query.list.ListProjectsQueryHandler;
import serp.project.pmcore.application.project.query.list.ProjectSummaryView;
import serp.project.pmcore.application.shared.pagination.PageView;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.project.dto.request.CreateProjectRequest;
import serp.project.pmcore.ui.rest.project.dto.request.UpdateProjectRequest;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PROJECTS)
@RequiredArgsConstructor
public class ProjectController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateProjectCommandHandler createProjectCommandHandler;
    private final ArchiveProjectCommandHandler archiveProjectCommandHandler;
    private final UnarchiveProjectCommandHandler unarchiveProjectCommandHandler;
    private final UpdateProjectCommandHandler updateProjectCommandHandler;
    private final GetProjectByIdQueryHandler getProjectByIdQueryHandler;
    private final GetProjectByKeyQueryHandler getProjectByKeyQueryHandler;
    private final ListProjectsQueryHandler listProjectsQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<CreateProjectResult>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        CreateProjectResult response = createProjectCommandHandler.handle(new CreateProjectCommand(
                request.getName(),
                request.getKey(),
                request.getDescription(),
                request.getProjectTypeKey(),
                request.getLeadUserId(),
                request.getCategoryId(),
                request.getBlueprintId(),
                request.getUrl(),
                request.getAvatarId(),
                request.getIssueTypeSchemeId(),
                request.getWorkflowSchemeId(),
                request.getFieldConfigSchemeId(),
                request.getIssueTypeScreenSchemeId(),
                request.getPermissionSchemeId(),
                request.getNotificationSchemeId(),
                request.getPrioritySchemeId(),
                request.getIssueSecuritySchemeId(),
                request.getProvisioningMode(),
                tenantId,
                userId
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<UpdateProjectResult>> updateProject(
            @PathVariable("id") Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        UpdateProjectResult response = updateProjectCommandHandler.handle(new UpdateProjectCommand(
                projectId,
                request.toData(),
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<GeneralResponse<UpdateProjectResult>> archiveProject(
            @PathVariable("id") Long projectId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        UpdateProjectResult response = archiveProjectCommandHandler.handle(new ArchiveProjectCommand(
                projectId,
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PostMapping("/{id}/unarchive")
    public ResponseEntity<GeneralResponse<UpdateProjectResult>> unarchiveProject(
            @PathVariable("id") Long projectId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        UpdateProjectResult response = unarchiveProjectCommandHandler.handle(new UnarchiveProjectCommand(
                projectId,
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectDetailView>> getProjectById(
            @PathVariable("id") Long projectId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        ProjectDetailView response = getProjectByIdQueryHandler.handle(new GetProjectByIdQuery(projectId, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<PageView<ProjectSummaryView>>> listProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String projectTypeKey,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        PageView<ProjectSummaryView> response = listProjectsQueryHandler.handle(new ListProjectsQuery(
                tenantId,
                userId,
                authUtils.getCurrentGroups(),
                search,
                categoryId,
                projectTypeKey,
                archived,
                page,
                pageSize,
                sortBy,
                sortDirection
        ));

        return ResponseEntity.ok(responseUtils.success(response));
    }

    @GetMapping("/key/{key}")
    public ResponseEntity<GeneralResponse<ProjectDetailView>> getProjectByKey(
            @PathVariable String key) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        ProjectDetailView response = getProjectByKeyQueryHandler.handle(new GetProjectByKeyQuery(key, tenantId));
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
