/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.permission.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.permission.ProjectPermissionDefinitionView;
import serp.project.pmcore.application.project.permission.ProjectPermissionGrantView;
import serp.project.pmcore.application.project.permission.ProjectPermissionSchemeView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.permission.entity.PermissionSchemeEntity;
import serp.project.pmcore.domain.permission.port.IPermissionDefinitionPort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemeEntryPort;
import serp.project.pmcore.domain.permission.port.IPermissionSchemePort;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.dto.ProjectPermissionSubject;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.project.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class GetProjectPermissionSettingsQueryHandler
        implements IQueryHandler<GetProjectPermissionSettingsQuery, ProjectPermissionSettingsView> {

    private final IProjectService projectService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;
    private final IPermissionSchemePort permissionSchemePort;
    private final IPermissionSchemeEntryPort permissionSchemeEntryPort;
    private final IPermissionDefinitionPort permissionDefinitionPort;

    @Override
    @Transactional(readOnly = true)
    public ProjectPermissionSettingsView handle(GetProjectPermissionSettingsQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                ProjectPermissionSubject.from(project),
                ProjectPermissionEvaluationContext.builder()
                        .userId(query.userId())
                        .groupKeys(query.groupKeys())
                        .build(),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        Long schemeId = project.getPermissionSchemeId();
        if (schemeId == null) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PERMISSION_SCHEME_NOT_FOUND,
                    "Project has no permission scheme: projectId=" + project.getId()
            );
        }

        PermissionSchemeEntity scheme = permissionSchemePort
                .getPermissionSchemeByIdIncludingSystem(schemeId, query.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.PERMISSION_SCHEME_NOT_FOUND,
                        "Permission scheme not found: id=" + schemeId
                ));

        return new ProjectPermissionSettingsView(
                ProjectPermissionSchemeView.from(scheme, query.tenantId()),
                permissionDefinitionPort.getPermissionDefinitionsIncludingSystem(query.tenantId())
                        .stream()
                        .map(ProjectPermissionDefinitionView::from)
                        .toList(),
                permissionSchemeEntryPort.getPermissionSchemeEntriesBySchemeIdIncludingSystem(schemeId, query.tenantId())
                        .stream()
                        .map(ProjectPermissionGrantView::from)
                        .toList()
        );
    }
}
