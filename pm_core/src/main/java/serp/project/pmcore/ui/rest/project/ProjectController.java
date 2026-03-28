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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.project.command.create.CreateProjectCommand;
import serp.project.pmcore.application.project.command.create.CreateProjectCommandHandler;
import serp.project.pmcore.application.project.command.create.CreateProjectResult;
import serp.project.pmcore.application.project.query.get.GetProjectByIdQuery;
import serp.project.pmcore.application.project.query.get.GetProjectByIdQueryHandler;
import serp.project.pmcore.application.project.query.get.GetProjectByKeyQuery;
import serp.project.pmcore.application.project.query.get.GetProjectByKeyQueryHandler;
import serp.project.pmcore.application.project.query.get.ProjectDetailView;
import serp.project.pmcore.domain.constant.RestControllerConstants;
import serp.project.pmcore.domain.exception.AccessDeniedException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.project.dto.request.CreateProjectRequest;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(RestControllerConstants.PROJECTS)
@RequiredArgsConstructor
public class ProjectController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final CreateProjectCommandHandler createProjectCommandHandler;
    private final GetProjectByIdQueryHandler getProjectByIdQueryHandler;
    private final GetProjectByKeyQueryHandler getProjectByKeyQueryHandler;

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

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<ProjectDetailView>> getProjectById(
            @PathVariable("id") Long projectId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        ProjectDetailView response = getProjectByIdQueryHandler.handle(new GetProjectByIdQuery(projectId, tenantId));
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
