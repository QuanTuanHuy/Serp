/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.project.permission;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.project.permission.command.ReplaceProjectPermissionGrantsCommand;
import serp.project.pmcore.application.project.permission.command.ReplaceProjectPermissionGrantsCommandHandler;
import serp.project.pmcore.application.project.permission.query.GetProjectPermissionSettingsQuery;
import serp.project.pmcore.application.project.permission.query.GetProjectPermissionSettingsQueryHandler;
import serp.project.pmcore.application.project.permission.query.ProjectPermissionSettingsView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.project.permission.dto.request.ReplaceProjectPermissionGrantsRequest;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

@RestController
@RequestMapping(PathConstants.PROJECT_PERMISSIONS)
@RequiredArgsConstructor
public class ProjectPermissionController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;
    private final GetProjectPermissionSettingsQueryHandler getProjectPermissionSettingsQueryHandler;
    private final ReplaceProjectPermissionGrantsCommandHandler replaceProjectPermissionGrantsCommandHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<ProjectPermissionSettingsView>> getProjectPermissions(
            @PathVariable Long projectId) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        ProjectPermissionSettingsView response = getProjectPermissionSettingsQueryHandler.handle(
                new GetProjectPermissionSettingsQuery(
                        projectId,
                        tenantId,
                        userId,
                        authUtils.getCurrentGroups()
                )
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }

    @PutMapping("/grants")
    public ResponseEntity<GeneralResponse<ProjectPermissionSettingsView>> replaceProjectPermissionGrants(
            @PathVariable Long projectId,
            @Valid @RequestBody ReplaceProjectPermissionGrantsRequest request) {
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.USER_NOT_FOUND));
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        ProjectPermissionSettingsView response = replaceProjectPermissionGrantsCommandHandler.handle(
                new ReplaceProjectPermissionGrantsCommand(
                        projectId,
                        tenantId,
                        userId,
                        authUtils.getCurrentGroups(),
                        request.toGrantData()
                )
        );
        return ResponseEntity.ok(responseUtils.success(response));
    }
}
