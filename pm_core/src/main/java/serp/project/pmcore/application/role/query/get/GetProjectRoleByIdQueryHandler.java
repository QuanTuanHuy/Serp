/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.role.query.get;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.pmcore.application.role.ProjectRoleView;
import serp.project.pmcore.application.shared.cqrs.query.IQueryHandler;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectRoleService;

@Service
@RequiredArgsConstructor
public class GetProjectRoleByIdQueryHandler implements IQueryHandler<GetProjectRoleByIdQuery, ProjectRoleView> {

    private final IProjectRoleService projectRoleService;

    @Override
    @Transactional(readOnly = true)
    public ProjectRoleView handle(GetProjectRoleByIdQuery query) {
        ProjectRoleEntity role = projectRoleService.getProjectRoleByIdIncludingSystem(query.roleId(), query.tenantId());
        return ProjectRoleView.from(role, Boolean.TRUE.equals(role.getIsSystem()));
    }
}
