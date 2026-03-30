/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.role;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import serp.project.pmcore.application.role.query.list.ListProjectRoleQuery;
import serp.project.pmcore.application.role.query.list.ListProjectRoleQueryHandler;
import serp.project.pmcore.application.role.query.list.ProjectRoleView;
import serp.project.pmcore.domain.shared.exception.AccessDeniedException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.kernel.utils.AuthUtils;
import serp.project.pmcore.ui.rest.shared.constant.PathConstants;
import serp.project.pmcore.ui.rest.shared.response.GeneralResponse;
import serp.project.pmcore.ui.rest.shared.response.ResponseUtils;

import java.util.List;

@RestController
@RequestMapping(PathConstants.ROLES)
@RequiredArgsConstructor
public class ProjectRoleController {

    private final AuthUtils authUtils;
    private final ResponseUtils responseUtils;

    private final ListProjectRoleQueryHandler listProjectRoleQueryHandler;

    @GetMapping
    public ResponseEntity<GeneralResponse<List<ProjectRoleView>>> getProjectRoles() {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AccessDeniedException(DomainErrorCode.TENANT_NOT_FOUND));

        List<ProjectRoleView> roles = listProjectRoleQueryHandler.handle(
                new ListProjectRoleQuery(tenantId)
        );
        return ResponseEntity.ok(responseUtils.success(roles));
    }
}
