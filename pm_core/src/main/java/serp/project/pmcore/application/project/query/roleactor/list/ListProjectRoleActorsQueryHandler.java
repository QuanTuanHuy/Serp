/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.roleactor.list;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.project.roleactor.model.ProjectRoleActorView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.dto.ProjectPermissionEvaluationContext;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.service.IProjectPermissionEvaluationService;
import serp.project.pmcore.domain.service.IProjectRoleActorService;
import serp.project.pmcore.domain.service.IProjectRoleService;
import serp.project.pmcore.domain.service.IProjectService;
import serp.project.pmcore.domain.shared.constant.ProjectPermissionKeys;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListProjectRoleActorsQueryHandler
        implements IQueryHandler<ListProjectRoleActorsQuery, List<ProjectRoleActorView>> {

    private final IProjectService projectService;
    private final IProjectRoleService projectRoleService;
    private final IProjectRoleActorService projectRoleActorService;
    private final IProjectPermissionEvaluationService projectPermissionEvaluationService;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectRoleActorView> handle(ListProjectRoleActorsQuery query) {
        ProjectEntity project = projectService.getProjectById(query.projectId(), query.tenantId());
        projectPermissionEvaluationService.checkPermission(
                project,
                buildEvaluationContext(query.userId(), query.groupKeys()),
                ProjectPermissionKeys.ADMINISTER_PROJECTS
        );

        projectRoleService.getProjectRoleByIdIncludingSystem(query.roleId(), query.tenantId());
        return projectRoleActorService.getActorsByProjectAndRole(query.projectId(), query.roleId(), query.tenantId())
                .stream()
                .map(ProjectRoleActorView::from)
                .toList();
    }

    private ProjectPermissionEvaluationContext buildEvaluationContext(Long userId, Set<String> groupKeys) {
        return ProjectPermissionEvaluationContext.builder()
                .userId(userId)
                .groupKeys(groupKeys)
                .build();
    }
}
