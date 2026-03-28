/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.roleactor;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.project.command.roleactor.add.AddProjectRoleActorCommand;
import serp.project.pmcore.application.project.command.roleactor.add.AddProjectRoleActorCommandHandler;
import serp.project.pmcore.application.project.command.roleactor.remove.RemoveProjectRoleActorCommand;
import serp.project.pmcore.application.project.command.roleactor.remove.RemoveProjectRoleActorCommandHandler;
import serp.project.pmcore.application.project.query.roleactor.list.ListProjectRoleActorsQuery;
import serp.project.pmcore.application.project.query.roleactor.list.ListProjectRoleActorsQueryHandler;
import serp.project.pmcore.application.project.roleactor.model.ProjectRoleActorView;
import serp.project.pmcore.domain.constant.RestControllerConstants;
import serp.project.pmcore.domain.exception.AccessDeniedException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.project.roleactor.dto.request.AddProjectRoleActorRequest;
import serp.project.pmcore.ui.rest.project.roleactor.dto.response.ProjectRoleActorResponse;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(RestControllerConstants.PROJECT_ROLE_ACTORS)
@RequiredArgsConstructor
public class ProjectRoleActorController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final AddProjectRoleActorCommandHandler addProjectRoleActorCommandHandler;
    private final RemoveProjectRoleActorCommandHandler removeProjectRoleActorCommandHandler;
    private final ListProjectRoleActorsQueryHandler listProjectRoleActorsQueryHandler;

    @PostMapping
    public ResponseEntity<GeneralResponse<ProjectRoleActorResponse>> addProjectRoleActor(
            @PathVariable Long projectId,
            @PathVariable Long roleId,
            @Valid @RequestBody AddProjectRoleActorRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        ProjectRoleActorView actor = addProjectRoleActorCommandHandler.handle(new AddProjectRoleActorCommand(
                projectId,
                roleId,
                request.getSubjectType(),
                request.getSubjectId(),
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        ));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseUtils.success(ProjectRoleActorResponse.from(actor)));
    }

    @DeleteMapping
    public ResponseEntity<GeneralResponse<?>> removeProjectRoleActor(
            @PathVariable Long projectId,
            @PathVariable Long roleId,
            @RequestParam String subjectType,
            @RequestParam String subjectId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        removeProjectRoleActorCommandHandler.handle(new RemoveProjectRoleActorCommand(
                projectId,
                roleId,
                subjectType,
                subjectId,
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        ));

        return ResponseEntity.ok(responseUtils.status("Project role actor removed"));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<List<ProjectRoleActorResponse>>> listProjectRoleActors(
            @PathVariable Long projectId,
            @PathVariable Long roleId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        List<ProjectRoleActorResponse> response = listProjectRoleActorsQueryHandler.handle(new ListProjectRoleActorsQuery(
                        projectId,
                        roleId,
                        tenantId,
                        userId,
                        authUtils.getCurrentGroups()
                ))
                .stream()
                .map(ProjectRoleActorResponse::from)
                .toList();

        return ResponseEntity.ok(responseUtils.success(response));
    }
}
