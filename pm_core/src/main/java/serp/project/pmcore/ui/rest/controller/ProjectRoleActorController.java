/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.controller;

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
import serp.project.pmcore.application.command.projectroleactor.AddProjectRoleActorCommand;
import serp.project.pmcore.application.command.projectroleactor.RemoveProjectRoleActorCommand;
import serp.project.pmcore.application.query.projectroleactor.ListProjectRoleActorsQuery;
import serp.project.pmcore.domain.constant.RestControllerConstants;
import serp.project.pmcore.domain.dto.request.project.AddProjectRoleActorRequest;
import serp.project.pmcore.domain.dto.response.GeneralResponse;
import serp.project.pmcore.domain.exception.AccessDeniedException;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.kernel.utils.ResponseUtils;

@RestController
@RequestMapping(RestControllerConstants.PROJECT_ROLE_ACTORS)
@RequiredArgsConstructor
public class ProjectRoleActorController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final AddProjectRoleActorCommand addProjectRoleActorCommand;
    private final RemoveProjectRoleActorCommand removeProjectRoleActorCommand;
    private final ListProjectRoleActorsQuery listProjectRoleActorsQuery;

    @PostMapping
    public ResponseEntity<GeneralResponse<?>> addProjectRoleActor(@PathVariable Long projectId,
                                                                  @PathVariable Long roleId,
                                                                  @Valid @RequestBody AddProjectRoleActorRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        var response = addProjectRoleActorCommand.execute(
                projectId,
                roleId,
                request,
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(responseUtils.success(response));
    }

    @DeleteMapping
    public ResponseEntity<GeneralResponse<?>> removeProjectRoleActor(@PathVariable Long projectId,
                                                                     @PathVariable Long roleId,
                                                                     @RequestParam String subjectType,
                                                                     @RequestParam String subjectId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        removeProjectRoleActorCommand.execute(
                projectId,
                roleId,
                subjectType,
                subjectId,
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        );

        return ResponseEntity.ok(responseUtils.status("Project role actor removed"));
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<?>> listProjectRoleActors(@PathVariable Long projectId,
                                                                    @PathVariable Long roleId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        var response = listProjectRoleActorsQuery.execute(
                projectId,
                roleId,
                tenantId,
                userId,
                authUtils.getCurrentGroups()
        );

        return ResponseEntity.ok(responseUtils.success(response));
    }
}
